package net.ximatai.muyun.gateway.routes;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public abstract class BaseRoute implements Handler<RoutingContext> {

    protected String path;
    protected boolean protect;
    protected boolean regex;
    protected List<String> whitelist;
    protected List<String> noStore;

    public String getPath() {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path is blank");
        }

        return burgerPath(path);
    }

    public boolean isProtect() {
        return protect;
    }

    public boolean isRegex() {
        return regex;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public List<String> getNoStore() {
        return noStore;
    }

    public void mount(Router router, Handler<RoutingContext>... handler) {
        Route route;
        if (regex) {
            route = router.routeWithRegex(path);
        } else {
            route = router.route(getPath() + "*");
        }
        for (Handler<RoutingContext> routingContextHandler : handler) {
            route.handler(routingContextHandler);
        }
        route.handler(this);
    }

    static String burgerPath(String path) {
        path = path.trim();
        if (!path.startsWith("/")) path = "/" + path;
        if (!path.endsWith("/")) path = path + "/";

        return path;
    }
}
