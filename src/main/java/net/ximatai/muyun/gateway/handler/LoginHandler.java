package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler implements Handler<RoutingContext> {
    private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    private final String loginApi;
    private final WebClient webClient;

    public static final String USER_SESSION = "gateway.user";

    private LoginHandler(Vertx vertx, String api) {
        this.loginApi = api;

        WebClientOptions options = new WebClientOptions()
                .setIdleTimeout(10)
                .setKeepAlive(false)
                .setConnectTimeout(10_000);
        webClient = WebClient.create(vertx, options);
    }

    public static LoginHandler create(Vertx vertx, String api) {
        return new LoginHandler(vertx, api);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        Session session = routingContext.session();

        MultiMap headers = request.headers();
        String host = request.remoteAddress().host();
        if (headers.contains("X-Forwarded-For")) host = request.getHeader("X-Forwarded-For");
        if (headers.contains("X-Real-IP")) host = request.getHeader("X-Real-IP");

        JsonObject body = routingContext.body().asJsonObject();
        webClient.postAbs(loginApi)
                .putHeader("X-Forwarded-For", host)
                .putHeader("User-Agent", headers.get("User-Agent"))
                .putHeader("Cookie", headers.get("Cookie"))
                .sendJson(body)
                .onSuccess(event -> {
                    if (event.statusCode() == 200) {
                        JsonObject user = event.bodyAsJsonObject();
                        session.put(USER_SESSION, user);
                        response
                                .putHeader("content-type", "application/json")
                                .send(user.encode());
                    } else {
                        handleFailure(event.bodyAsString(), null, response);
                    }
                })
                .onFailure(event -> {
                    handleFailure("登录接口访问出错", event, response);
                });

    }

    private void handleFailure(String msg, Throwable e, HttpServerResponse response) {
        logger.error(msg, e);
        response.setStatusCode(500)
                .putHeader("Content-Type", "text/plain; charset=utf-8")
                .end(msg);
    }
}
