package net.ximatai.muyun.gateway.routes;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public interface IBaseRoute extends Handler<RoutingContext> {

    String path();

    boolean protect();

    boolean regex();

    List<String> whiteList();

    List<String> noStore();

    default void mount(Router router, Handler<RoutingContext>... handler) {
        Route route;
        if (regex()) {
            route = router.routeWithRegex(path());
        } else {
            route = router.route(burgerPath() + "*");
        }
        for (Handler<RoutingContext> routingContextHandler : handler) {
            route.handler(routingContextHandler);
        }
        route.handler(this);
    }

    default String burgerPath() {
        String path = path();
        if (!path.startsWith("/")) path = "/" + path;
        if (!path.endsWith("/")) path = path + "/";

        return path;
    }
}
