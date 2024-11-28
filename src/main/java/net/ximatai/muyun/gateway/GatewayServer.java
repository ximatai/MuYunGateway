package net.ximatai.muyun.gateway;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.ximatai.muyun.gateway.config.model.GatewayConfigDto;
import net.ximatai.muyun.gateway.handler.AuthHandler;
import net.ximatai.muyun.gateway.handler.FrontendHandler;
import net.ximatai.muyun.gateway.handler.LoginHandler;
import net.ximatai.muyun.gateway.handler.NoCacheHandler;
import net.ximatai.muyun.gateway.routes.IBaseRouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.ximatai.muyun.gateway.RoutingContextKeyConst.IS_REROUTE;
import static net.ximatai.muyun.gateway.RoutingContextKeyConst.REROUTE_REASON;

@Singleton
public class GatewayServer {
    private final Logger logger = LoggerFactory.getLogger(GatewayServer.class);

    @Inject
    Vertx vertx;

    private Router router;
    private HttpServer server;
    private GatewayConfigDto gatewayConfig;

    private List<IBaseRouteHandler> routes = new ArrayList<>();

    private SessionStore store;
    private SessionHandler sessionHandler;

    @PostConstruct
    void init() {
        store = SessionStore.create(vertx);
        sessionHandler = SessionHandler.create(store)
                .setSessionCookieName("muyun-gateway")
                .setCookieHttpOnlyFlag(true);
    }

    public void register(GatewayConfigDto config) {
        routes.clear();

        if (this.gatewayConfig != null && server != null) {
            if (config.getPort() != this.gatewayConfig.getPort()) {
                logger.info("port changed {} -> {}", this.gatewayConfig.getPort(), config.getPort());
//                store.clear();
                this.stop();
            }
        }

        if (router != null) {
            router.getRoutes().forEach(Route::remove);
        }

        if (router == null || server == null) { // 说明 stop 了，router 需要重建
            router = Router.router(vertx);
        }

        this.gatewayConfig = config;
        int port = config.getPort();

        router.route().handler(sessionHandler);

        router.route("/").handler(this::indexHandler);
        router.route("/login")
                .handler(BodyHandler.create())
                .handler(LoginHandler.create(vertx, config.getLogin().api()));

        config.getRedirects().forEach(redirect -> {
            router.route(redirect.from())
                    .handler(rc -> {
                        rc.response().setStatusCode(301)
                                .putHeader("Location", redirect.to())
                                .end();
                    });
        });

        config.getFrontends().forEach(frontend -> {
            routes.add(frontend);
        });

        config.getUpstreams().forEach(upstream -> {
            routes.add(upstream);
        });

        routes.forEach(route -> {
            NoCacheHandler noCacheHandler = new NoCacheHandler(route.burgerPath(), route.noCache());
            route.mountTo(router, new AuthHandler(), noCacheHandler);
        });

        router.errorHandler(404, this::notFoundHandler);

        if (server == null) { // 初次启动或者stop之后。server 为 null
            server = vertx.createHttpServer(getServerOptions(config));
            server.requestHandler(router).listen(port)
                    .onSuccess(it -> {
                        logger.info("MuYunGateway-Proxy running on port {}", port);
                    })
                    .onFailure(event -> {
                        logger.error("MuYunGateway startup failed", event);
                    });
        }

    }

    private void indexHandler(RoutingContext routingContext) {
        String index = gatewayConfig.getIndex();
        if ("/".equals(index)) {
            routingContext.next();
        } else {
            routingContext.response()
                    .setStatusCode(302)
                    .putHeader("Location", index)
                    .end();
        }
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

        for (IBaseRouteHandler route : routes) {
            if (route instanceof FrontendHandler frontendRoute) {
                if (frontendRoute.isNotFoundReroute() && path.startsWith(frontendRoute.burgerPath())) {
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
            response
                    .setStatusCode(404)
                    .putHeader("Context-type", "text/plain")
                    .end("Resource not found");
        }
    }

    private HttpServerOptions getServerOptions(GatewayConfigDto config) {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setMaxHeaderSize(64 * 1024);
        serverOptions.setCompressionSupported(true);
        serverOptions.setReusePort(false);

        GatewayConfigDto.SslConfig sslConfig = config.getSsl();
        if (sslConfig.use()) {
            String certPath = sslConfig.certPath();
            String keyPath = sslConfig.keyPath();
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions().setCertPath(certPath).setKeyPath(keyPath);
            serverOptions.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }
        return serverOptions;
    }

    synchronized private boolean stop() {
        CompletableFuture<Boolean> future = new CompletableFuture();
        if (server != null) {
            server.close().onSuccess(event -> {
                        future.complete(true);
                    })
                    .onFailure(future::completeExceptionally);
        }
        server = null;

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public GatewayConfigDto getGatewayConfig() {
        return gatewayConfig;
    }

}
