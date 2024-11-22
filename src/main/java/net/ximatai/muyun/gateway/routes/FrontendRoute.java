package net.ximatai.muyun.gateway.routes;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

public class FrontendRoute extends BaseRoute implements Handler<RoutingContext> {

    private String dir;
    private String notFoundReroute;
    private Handler<RoutingContext> staticHandler;

    public FrontendRoute(String path) {
        super(path);
    }

    public String getDir() {
        return dir;
    }

    public FrontendRoute setDir(String dir) {
        this.dir = dir.trim();
        return this;
    }

    public String getNotFoundReroute() {
        return notFoundReroute;
    }

    public FrontendRoute setNotFoundReroute(String notFoundReroute) {
        this.notFoundReroute = notFoundReroute.trim();
        return this;
    }

    public Handler<RoutingContext> getStaticHandler() {
        if (staticHandler == null) {
            staticHandler = StaticHandler.create(FileSystemAccess.ROOT, dir)
                    .setCachingEnabled(false)
                    .setMaxAgeSeconds(7 * 24 * 60 * 60 * 1000)
                    .setCacheEntryTimeout(30_000)
                    .setFilesReadOnly(false);
        }
        return staticHandler;

    }

    public boolean isNotFoundReroute() {
        return notFoundReroute != null && !notFoundReroute.isBlank();
    }

    public String reroutePath() {
        if (notFoundReroute.startsWith("/")) {
            return getPath() + notFoundReroute.substring(1);
        } else {
            return getPath() + notFoundReroute;
        }
    }

    @Override
    public void handle(RoutingContext routingContext) {
        routingContext.response().headers().remove(HttpHeaders.CONTENT_ENCODING);
        getStaticHandler().handle(routingContext);
    }
}
