package net.ximatai.muyun.gateway;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.handler.Backend;
import net.ximatai.muyun.gateway.handler.UpstreamHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TestUpstream {

    @Inject
    GatewayServer gatewayServer;

    private int port;

    public TestUpstream() {
        Vertx vertx = Vertx.vertx();
        boolean backendStarted;
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        Router router = Router.router(vertx);

        router.get("/").handler(routingContext -> {
            routingContext.response().setStatusCode(200).end("Hello, World!");
        });

        router.get("/test1").handler(routingContext -> {
            routingContext.response().setStatusCode(200).end("Hello, World! TEST1!");
        });

        router.get("/test2").handler(routingContext -> {
            routingContext.response().setStatusCode(200).end("Hello, World! TEST2!");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0)
                .onComplete(event -> {
                    port = event.result().actualPort();
                    completableFuture.complete(event.succeeded());
                });
        try {
            backendStarted = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(backendStarted);
    }

    @Test
    public void testBackendStarted() {
        Assertions.assertTrue(port > 0);

        String string = given()
                .get("http://localhost:%s/".formatted(port))
                .then()
                .statusCode(200)
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testUpstream() {
        UpstreamHandler upstreamHandler = new UpstreamHandler("/test", false, false, "", List.of(), List.of(), List.of(
                new Backend("http://localhost:%s/".formatted(port), 1)
        ));

        registerServer(upstreamHandler);

        String string = given()
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testUpstream404() {
        UpstreamHandler upstreamHandler = new UpstreamHandler("/test", false, false, "", List.of(), List.of(), List.of(
                new Backend("http://localhost:%s/".formatted(port), 1)
        ));

        registerServer(upstreamHandler);

        given()
                .get("http://localhost:9999/test/xxx")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpstreamSecured() {
        UpstreamHandler upstreamHandler = new UpstreamHandler("/test", true, false, "", List.of(), List.of(), List.of(
                new Backend("http://localhost:%s/".formatted(port), 1)
        ));

        registerServer(upstreamHandler);

        given()
                .redirects().follow(false)
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(302)
                .header("Location", "/login.html");
    }

    @Test
    public void testUpstreamSecuredAllow() {
        UpstreamHandler upstreamHandler = new UpstreamHandler("/test", true, false, "", List.of(), List.of(
                "/xxx",
                "/test.*"
        ), List.of(
                new Backend("http://localhost:%s/".formatted(port), 1)
        ));

        registerServer(upstreamHandler);

        given()
                .get("http://localhost:9999/test/xxx")
                .then()
                .statusCode(404);

        given()
                .get("http://localhost:9999/test/test1")
                .then()
                .statusCode(200);

        given()
                .get("http://localhost:9999/test/test2")
                .then()
                .statusCode(200);

        given()
                .get("http://localhost:9999/test/test3")
                .then()
                .statusCode(404);
    }

    private void registerServer(UpstreamHandler upstreamHandler) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        gatewayServer.register(buildConfig(upstreamHandler))
                .onComplete(event -> completableFuture.complete(event.succeeded()));

        Boolean registerSuccess;
        try {
            registerSuccess = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(registerSuccess);
    }

    private GatewayConfig buildConfig(UpstreamHandler upstreamHandler) {
        return new GatewayConfig(
                9999,
                "/",
                new GatewayConfig.SslConfig(false, null, null),
                new GatewayConfig.LoginConfig("/login", "/login.html", null),
                new GatewayConfig.JwtConfig(false, false, null),
                new GatewayConfig.SessionConfig(false, 1),
                List.of(),
                List.of(),
                List.of(
                        upstreamHandler
                )
        );
    }
}
