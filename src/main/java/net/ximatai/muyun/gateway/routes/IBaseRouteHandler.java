package net.ximatai.muyun.gateway.routes;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public interface IBaseRouteHandler extends Handler<RoutingContext> {

    String path();

    boolean secured();

    boolean regex();

    List<String> allowlist();

    List<String> noCache();

    default void mountTo(Router router, List<Handler<RoutingContext>> handlers) {
        Route route;
        if (regex()) {
            route = router.routeWithRegex(path());
        } else {
            route = router.route(routePath());
        }
        for (Handler<RoutingContext> routingContextHandler : handlers) {
            route.handler(routingContextHandler);
        }
        route.handler(this);
    }

    default String routePath() {
        return burgerPath() + "*";
    }

    default String burgerPath() {
        String path = path();
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("path :%s must start with '/'".formatted(path));
        }

        if (!path.endsWith("/")) path = path + "/";

        return path;
    }
}
