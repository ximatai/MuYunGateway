package net.ximatai.muyun.gateway.routes;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import net.ximatai.muyun.gateway.config.model.GatewayConfigDto;

public class FrontendRoute extends BaseRoute implements Handler<RoutingContext> {

    private String dir;
    private String notFoundReroute;
    private Handler<RoutingContext> staticHandler;

    public FrontendRoute(GatewayConfigDto.Frontend frontend) {
        this.path = frontend.getPath();
        this.protect = frontend.isProtect();
        this.regex = frontend.isRegex();
        this.whitelist = frontend.getWhiteList();
        this.noStore = frontend.getNoStore();

        this.dir = frontend.getDir();
        this.notFoundReroute = frontend.getNotFoundReroute();

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
        // 只有http 服务是quarkus负责启动的时候，需要使用下面的hack代码，see https://github.com/quarkusio/quarkus/issues/44637
        // routingContext.response().headers().remove(HttpHeaders.CONTENT_ENCODING);
        getStaticHandler().handle(routingContext);
    }
}
