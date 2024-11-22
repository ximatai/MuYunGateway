package net.ximatai.muyun.gateway;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.gateway.routes.BaseRoute;
import net.ximatai.muyun.gateway.routes.FrontendRoute;

import java.util.List;

import static net.ximatai.muyun.gateway.RoutingContextKeyConst.IS_REROUTE;
import static net.ximatai.muyun.gateway.RoutingContextKeyConst.REROUTE_REASON;

public class RouteRegister {

    FrontendRoute web = new FrontendRoute("web")
            .setDir("/Users/aruis/develop/workspace-github/MuYunGateway/src/test/resources/webroot/")
            .setNotFoundReroute("/");

    List<BaseRoute> routes = List.of(web);

    public RouteRegister(Router router) {
        routes.forEach(route -> {
            route.mount(router);
        });

        router.errorHandler(404, this::notFoundHandler);
    }

    /**
     * 当发生404的时候，如果前端配置了重路由，需要特殊处理
     *
     * @param routingContext
     */
    private void notFoundHandler(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        String path = request.path();
        boolean hit = false;

        for (BaseRoute route : routes) {
            if (route instanceof FrontendRoute frontendRoute) {

                if (frontendRoute.isNotFoundReroute() && path.startsWith(frontendRoute.getPath())) {
                    String reroutePath = frontendRoute.reroutePath();
                    if (!path.equals(reroutePath)) {
                        hit = true;
                        routingContext.put(IS_REROUTE, true);  //标记request 被 reroute
                        routingContext.put(REROUTE_REASON, "frontend_router");
                        routingContext.reroute(reroutePath);
                    }
                }

            }
        }

        if (!hit) {
//            routingContext.next();
            response
                    .setStatusCode(404)
                    .putHeader("Context-type", "text/plain")
                    .end("Resource not found");
        }
    }

}
