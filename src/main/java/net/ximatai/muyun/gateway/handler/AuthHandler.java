package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.gateway.RoutingContextKeyConst;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

import static net.ximatai.muyun.gateway.handler.LoginHandler.USER_SESSION;

public class AuthHandler implements Handler<RoutingContext> {

    GatewayConfig config;

    public AuthHandler(GatewayConfig config) {
        this.config = config;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        if (config.getSession().use()) {
            Session session = routingContext.session();
            JsonObject user = session.get(USER_SESSION);
            routingContext.put(RoutingContextKeyConst.USER, user);
        }

        routingContext.next();
    }
}
