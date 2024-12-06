package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NoCacheHandler implements Handler<RoutingContext> {

    private final String burgerPath;
    private final List<String> noCachePaths;

    public NoCacheHandler(String burgerPath, List<String> noCache) {
        this.burgerPath = burgerPath;
        this.noCachePaths = new ArrayList<>();
        noCache.forEach(path -> {
            if (path != null) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                this.noCachePaths.add(this.burgerPath + path);
            }
        });
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String path = routingContext.request().path();
        boolean isNoStore = noCachePaths.stream()
                .anyMatch(noStorePath -> noStorePath.equals(path) || Pattern.matches(noStorePath, path));

        if (isNoStore) {
            routingContext.response()
                    .putHeader("cache-control", "no-cache, no-store");
        }
        routingContext.next();
    }
}
