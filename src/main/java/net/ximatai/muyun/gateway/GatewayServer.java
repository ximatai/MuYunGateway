package net.ximatai.muyun.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.ximatai.muyun.gateway.config.Management;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.handler.AllowListHandler;
import net.ximatai.muyun.gateway.handler.AuthHandler;
import net.ximatai.muyun.gateway.handler.FrontendHandler;
import net.ximatai.muyun.gateway.handler.HeaderHandler;
import net.ximatai.muyun.gateway.handler.LoginHandler;
import net.ximatai.muyun.gateway.handler.NoCacheHandler;
import net.ximatai.muyun.gateway.routes.IBaseRouteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.ximatai.muyun.gateway.RoutingContextKeyConst.*;

@Singleton
public class GatewayServer {
    private final Logger logger = LoggerFactory.getLogger(GatewayServer.class);

    public static String token;

    @Inject
    Vertx vertx;

    @Inject
    Management management;

    private Router router;
    private HttpServer server;
    private GatewayConfig gatewayConfig;

    private List<IBaseRouteHandler> routes = new ArrayList<>();

    private SessionStore store;
    private SessionHandler sessionHandler;

    private JWTOptions jwtOption;
    private JWTAuth jwtAuth;
    private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    @PostConstruct
    void init() {
        if (management.checkToken()) {
            token = UUID.randomUUID().toString().replace("-", "");
            logger.info("Management token: {}", token);
        }

        store = SessionStore.create(vertx);
    }

    public Future<Integer> register(GatewayConfig config) {

        try {
            logger.info("Config content is:\n {}", objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Promise<Integer> promise = Promise.promise();
        routes.clear();

        if (this.gatewayConfig != null && server != null) {
            if (config.getPort() != this.gatewayConfig.getPort()) {
                logger.info("port changed {} -> {}", this.gatewayConfig.getPort(), config.getPort());
                this.stop();
            }
        }

        if (router != null) {
            router.getRoutes().forEach(Route::remove);
        }

        if (router == null || server == null) { // 要么是新启动要么是 stop 了，router 需要重建
            router = Router.router(vertx);
        }

        this.gatewayConfig = config;
        int port = config.getPort();

        if (config.getSession().use()) {
            sessionHandler = SessionHandler.create(store)
                    .setSessionCookieName("muyun-gateway")
                    .setSessionTimeout(config.getSession().timeoutHour() * 60 * 60 * 1000)
                    .setCookieHttpOnlyFlag(true)
                    .setCookieSameSite(CookieSameSite.STRICT);
            router.route().handler(sessionHandler);
        }

        GatewayConfig.JwtConfig jwtConfig = config.getJwt();
        if (jwtConfig.use()) {
            jwtOption = new JWTOptions()
                    .setIgnoreExpiration(!jwtConfig.checkExpiration())
                    .setExpiresInMinutes(jwtConfig.expiresHour() * 60);

            JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                    .setJWTOptions(jwtOption)
                    .addPubSecKey(new PubSecKeyOptions()
                            .setAlgorithm("HS256")
                            .setBuffer("MuYun"));

            jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);
        }

        AuthHandler authHandler = new AuthHandler(config, jwtAuth);
        HeaderHandler headerHandler = new HeaderHandler(config);
        router.route().handler(headerHandler);

        router.route("/").handler(this::indexHandler);

        String loginPath = config.getLogin().path();

        if (loginPath != null && !loginPath.isBlank()) {
            router.post(loginPath)
                    .handler(BodyHandler.create())
                    .handler(new LoginHandler(config, vertx, jwtAuth));
        }

        router.route("/logout").handler(this::logoutHandler);

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
            List<Handler<RoutingContext>> handlers = new ArrayList<>();
            handlers.add(new NoCacheHandler(route.burgerPath(), route.noCache()));

            if (route.secured()) {
                List<String> allowList = route.allowlist().stream().map(it -> {
                    if (it.startsWith("/")) {
                        it = it.substring(1);
                    }
                    return route.burgerPath() + it;
                }).toList();

                handlers.add(new AllowListHandler(allowList));
                handlers.add(authHandler);
            }

            route.mountTo(router, handlers);
        });

        router.errorHandler(404, this::notFoundHandler);

        if (server == null) { // 初次启动或者stop之后。server 为 null
            server = vertx.createHttpServer(getServerOptions(config));
            server.requestHandler(router).listen(port)
                    .onSuccess(it -> {
                        logger.info("MuYunGateway-Proxy running on port {}", it.actualPort());
                        promise.complete(it.actualPort());
                    })
                    .onFailure(event -> {
                        logger.error("MuYunGateway startup failed", event);
                        server = null;
                        promise.fail(event);
                    });
        } else {
            return Future.succeededFuture(config.getPort());
        }

        return promise.future();
    }

    private void logoutHandler(RoutingContext routingContext) {
        routingContext.session().destroy();
        routingContext.response()
                .setStatusCode(302)
                .putHeader("Location", gatewayConfig.getIndex())
                .end();
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
                if (frontendRoute.useNotFoundReroute() && path.startsWith(frontendRoute.burgerPath())) {
                    String reroutePath = frontendRoute.reroutePath();
                    if (!path.equals(reroutePath)) {
                        hit = true;
                        routingContext.put(IS_REROUTE, true);  //标记request 被 reroute
                        routingContext.put(REROUTE_REASON, VAL_REROUTE_REASON_FRONTEND);
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

    private HttpServerOptions getServerOptions(GatewayConfig config) {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setMaxHeaderSize(64 * 1024);
        serverOptions.setCompressionSupported(true);
        serverOptions.setReusePort(false);

        GatewayConfig.SslConfig sslConfig = config.getSsl();
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

    public GatewayConfig getGatewayConfig() {
        return gatewayConfig;
    }

}
