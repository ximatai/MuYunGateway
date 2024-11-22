package net.ximatai.muyun.gateway.routes;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

public abstract class BaseRoute implements Handler<RoutingContext> {

    protected String path;
    protected boolean isProtect;
    protected boolean isRegex;
    protected Set<String> whitelist;
    protected Set<String> noStoreList;

    public BaseRoute(String path) {
        this.path = path.trim();
    }

    public String getPath() {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path is blank");
        }

        return burgerPath(path) + "*";
    }

    public boolean isProtect() {
        return isProtect;
    }

    public boolean isRegex() {
        return isRegex;
    }

    public Set<String> getWhitelist() {
        return whitelist;
    }

    public Set<String> getNoStoreList() {
        return noStoreList;
    }

    public void mount(Router router,Handler<RoutingContext> ...handler) {
        Route route;
        if (isRegex) {
            route = router.routeWithRegex(path);
        } else {
            route = router.route(getPath());
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
