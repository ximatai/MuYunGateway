package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NoStoreHandler implements Handler<RoutingContext> {

    private final String burgerPath;
    private final List<String> noStorePaths;

    public NoStoreHandler(String burgerPath, List<String> noStore) {
        this.burgerPath = burgerPath;
        this.noStorePaths = new ArrayList<>();
        noStore.forEach(path -> {
            if (path != null) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                this.noStorePaths.add(this.burgerPath + path);
            }
        });
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String path = routingContext.request().path();
        boolean isNoStore = noStorePaths.stream()
                .anyMatch(noStorePath -> noStorePath.equals(path) || Pattern.matches(noStorePath, path));

        if (isNoStore) {
            routingContext.response()
                    .putHeader("cache-control", "no-cache,no-store");
        }
        routingContext.next();
    }
}
