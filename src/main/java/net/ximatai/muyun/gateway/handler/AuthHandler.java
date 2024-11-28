package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.gateway.RoutingContextKeyConst;

import static net.ximatai.muyun.gateway.handler.LoginHandler.USER_SESSION;

public class AuthHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        Session session = routingContext.session();

        JsonObject user = session.get(USER_SESSION);
        routingContext.put(RoutingContextKeyConst.USER, user);

        routingContext.next();
    }
}
