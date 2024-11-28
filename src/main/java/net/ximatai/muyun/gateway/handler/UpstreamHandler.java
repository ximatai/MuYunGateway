package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.gateway.routes.IBaseRouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
        HttpServerResponse resp = routingContext.response();
        Vertx vertx = routingContext.vertx();

        req.pause();

        Backend backend = backends.getFirst();

        String uri = req.uri().replaceFirst(path, backend.path());
        HttpClient backendClient = backend.getClient(vertx);

        String upgrade = req.getHeader("Upgrade");
        if ("websocket".equalsIgnoreCase(upgrade)) {
            Future<ServerWebSocket> fut = req.toWebSocket();
            fut.onSuccess(ws -> {
                WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions();
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
                    reqUpstream.headers().setAll(req.headers().remove("host"));

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

    void error(HttpServerResponse resp, Throwable err) {
        logger.error(err.getMessage(), err);
        resp.setStatusCode(500).end(err.getMessage());
    }

}


