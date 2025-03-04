package net.ximatai.muyun.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

public class HeaderHandler implements Handler<RoutingContext> {

    private final GatewayConfig config;

    public HeaderHandler(GatewayConfig config) {
        this.config = config;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        MultiMap headers = routingContext.response().headers();
        config.getHeaders().forEach(h -> headers.add(h.name(), h.value()));

        routingContext.next();
    }
}
