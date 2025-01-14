package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.gateway.RoutingContextKeyConst;
import net.ximatai.muyun.gateway.routes.IBaseRouteHandler;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record UpstreamHandler(String path, boolean secured, boolean regex, String comment, List<String> noCache,
                              List<String> allowlist, List<Backend> backends) implements IBaseRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpstreamHandler.class);

    @Override
    public String routePath() {
        if (backends.getFirst().getUrl().endsWith("/")) {
            return IBaseRouteHandler.super.routePath();
        } else {
            return burgerPath().substring(0, burgerPath().lastIndexOf("/"));
        }
    }

    private Backend getOnlineBackend() {
        List<Backend> onlineList = backends.stream().filter(Backend::isOnline).toList();
        if (onlineList.isEmpty()) {
            throw new RuntimeException("No online backend available @ %s".formatted(path));
        }

        List<Backend> swap = new ArrayList<>();

        // 按权重展开 swap list，之后从里面随机一个即可
        onlineList.forEach(backend ->
                swap.addAll(Collections.nCopies(backend.getWeight(), backend))
        );

        return swap.get(ThreadLocalRandom.current().nextInt(swap.size()));
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
        HttpServerResponse resp = routingContext.response();
        Vertx vertx = routingContext.vertx();

        req.pause();

        Backend backend = getOnlineBackend();
        String uri = req.uri().replaceFirst(path, backend.path()).replace("//", "/");
        HttpClient backendClient = backend.getClient(vertx);

        if (req.headers().contains("Upgrade", "websocket", true)) {
            Future<ServerWebSocket> fut = req.toWebSocket();
            fut.onSuccess(ws -> {
                WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions();
                webSocketConnectOptions.setHost(backend.getHost());
                webSocketConnectOptions.setPort(backend.getPort());
                webSocketConnectOptions.setURI(uri);
                webSocketConnectOptions.setHeaders(
                        req.headers()
                                .remove("host")
//                                            .remove("sec-websocket-extensions")
                );

                vertx.createWebSocketClient()
                        .connect(webSocketConnectOptions)
                        .onSuccess(clientWS -> {
                            ws.frameHandler(clientWS::writeFrame);
                            ws.closeHandler(x -> {
                                clientWS.close();
                            });
                            clientWS.frameHandler(ws::writeFrame);
                            clientWS.closeHandler(x -> {
                                ws.close();
                            });
                        }).onFailure(err -> {
                            error(resp, err);
                        });

            }).onFailure(err -> {
                error(resp, err);
            });
        } else {
            backendClient.request(req.method(), uri, ar -> {
                if (ar.succeeded()) {
                    HttpClientRequest reqUpstream = ar.result();
                    MultiMap headers = reqUpstream.headers();
                    headers.setAll(req.headers().remove("host"));

                    JsonObject user = routingContext.get(RoutingContextKeyConst.USER);
                    if (user != null) {
                        String userJson = user.encode();
                        String userBase64 = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));
                        String sign = DigestUtils.md5Hex(userJson + "BSY");

                        headers.set("gw-user", userBase64);
                        headers.set("gw-user-sign", sign);
                    }

                    String forwarded = req.getHeader("Forwarded");
                    String newForwarded = String.format(
                            "for=\"%s\"; proto=%s; host=\"%s\"",
                            req.remoteAddress().host(),  // 客户端 IP
                            req.scheme(),                // 请求协议
                            req.getHeader("Host")        // 请求目标主机
                    );

                    if (forwarded != null) {
                        forwarded += ", " + newForwarded;
                    } else {
                        forwarded = newForwarded;
                        headers.set("X-Forwarded-For", req.remoteAddress().host());
                    }

                    headers.set("Forwarded", forwarded);

                    reqUpstream.send(req)
                            .onSuccess(respUpstream -> {
                                resp.setStatusCode(respUpstream.statusCode());
                                resp.headers().setAll(respUpstream.headers());
                                resp.send(respUpstream);
                            }).onFailure(err -> {
                                error(resp, err);
                            });
                } else {
                    error(resp, ar.cause());
                }
            });
        }
    }

    private void error(HttpServerResponse resp, Throwable err) {
        logger.error(err.getMessage(), err);
        resp.setStatusCode(500).end(err.getMessage());
    }

}


