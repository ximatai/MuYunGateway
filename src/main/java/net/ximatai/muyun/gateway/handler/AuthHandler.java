package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.gateway.RoutingContextKeyConst;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

import java.util.Objects;

import static net.ximatai.muyun.gateway.RoutingContextKeyConst.*;
import static net.ximatai.muyun.gateway.handler.LoginHandler.USER_SESSION;

public class AuthHandler implements Handler<RoutingContext> {

    GatewayConfig config;

    public AuthHandler(GatewayConfig config) {
        this.config = config;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        boolean allowFlag = routingContext.get(ALLOW_FLAG, false)
                || Objects.equals(VAL_REROUTE_REASON_FRONTEND, routingContext.get(REROUTE_REASON));

        JsonObject user = null;

        if (config.getSession().use()) {
            Session session = routingContext.session();
            user = session.get(USER_SESSION);
        }

        if (user != null) {
            routingContext.put(RoutingContextKeyConst.USER, user);
        }

        if (allowFlag || user != null) {
            routingContext.next();
        } else {
            reject(routingContext);
        }

    }

    private void reject(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        Session session = routingContext.session();
        if (request.headers().contains("X-Requested-With", "XMLHttpRequest", true)) {
            response.setStatusCode(401).end();
        } else {
            if (config.getSession().use()) {
                session.put("target", request.uri());
            }

            response.setStatusCode(302)
                    .putHeader("Location", config.getLogin().page())
                    .end();
        }
    }
}
