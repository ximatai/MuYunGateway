package net.ximatai.muyun.gateway;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class NotFoundHandler {

    void init(@Observes StartupEvent ev, Router router) {
        // 捕获 404 错误，并重定向到前端的 index.html
        router.errorHandler(404, this::handle404);
    }

    private void handle404(RoutingContext context) {
        String path = context.normalizedPath();
        // 如果路径不包含扩展名，视为 SPA 路由请求
        if (!path.contains(".") && !path.startsWith("/api")) {
            context.response().setStatusCode(200).sendFile("META-INF/resources/gw/index.html");
        } else {
            // 保持其他请求的 404 返回
            context.response().setStatusCode(404).end("Resource not found.");
        }
    }
}
