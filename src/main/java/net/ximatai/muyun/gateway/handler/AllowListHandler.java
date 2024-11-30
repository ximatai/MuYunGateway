package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static net.ximatai.muyun.gateway.RoutingContextKeyConst.ALLOW_FLAG;

public class AllowListHandler implements Handler<RoutingContext> {
    private List<String> allowList;
    private List<Pattern> allowedPatterns = new ArrayList<>();

    private GatewayConfig config;

    public AllowListHandler(List<String> allowList) {
        this.allowList = allowList;
        allowedPatterns.addAll(allowList.stream().map(Pattern::compile).toList());
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String path = request.path();
        String uri = request.uri();

        boolean hit = allowList.stream().anyMatch(it -> path.equals(it) || uri.equals(it))
                || allowedPatterns.stream().anyMatch(p -> p.matcher(path).matches());

        if (hit) {
            routingContext.put(ALLOW_FLAG, true);
        }

        routingContext.next();
    }
}
