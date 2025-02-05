package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.gateway.RoutingContextKeyConst;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

import java.util.Objects;

import static net.ximatai.muyun.gateway.RoutingContextKeyConst.*;
import static net.ximatai.muyun.gateway.handler.LoginHandler.USER_SESSION;

public class AuthHandler implements Handler<RoutingContext> {

    private final GatewayConfig config;
    private final JWTAuth jwtAuth;

    public AuthHandler(GatewayConfig config, JWTAuth jwtAuth) {
        this.config = config;
        this.jwtAuth = jwtAuth;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        boolean allowFlag = routingContext.get(ALLOW_FLAG, false)
                || Objects.equals(VAL_REROUTE_REASON_FRONTEND, routingContext.get(REROUTE_REASON));

        Future.any(
                loadUserFromSession(routingContext),
                loadUserFromJwt(routingContext)
        ).onSuccess(compositeFuture -> {
            JsonObject user = compositeFuture.resultAt(0);
            if (user == null) {
                user = compositeFuture.resultAt(1);
            }
            routingContext
                    .put(RoutingContextKeyConst.USER, user)
                    .next();
        }).onFailure((throwable) -> {
            if (allowFlag) {
                routingContext.next();
            } else {
                reject(routingContext);
            }
        });

    }

    private Future<JsonObject> loadUserFromJwt(RoutingContext routingContext) {
        String authorizationHeader = routingContext.request().getHeader(HttpHeaders.AUTHORIZATION);

        if (jwtAuth == null || authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Future.failedFuture("JWT authentication failed");
        }

        String token = authorizationHeader.substring(7);
        return jwtAuth.authenticate(new TokenCredentials(token))
                .map(User::principal);
    }

    private Future<JsonObject> loadUserFromSession(RoutingContext routingContext) {
        if (!config.getSession().use()) {
            return Future.failedFuture("Session is disabled");
        }

        JsonObject user = routingContext.session().get(USER_SESSION);
        return user != null ? Future.succeededFuture(user) : Future.failedFuture("Session does not exist");
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
