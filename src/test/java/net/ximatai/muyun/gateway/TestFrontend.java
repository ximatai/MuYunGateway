package net.ximatai.muyun.gateway;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.handler.FrontendHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TestFrontend {

    private final Path tempDirectory;

    @Inject
    GatewayServer gatewayServer;

    public TestFrontend() throws IOException {
        tempDirectory = Files.createTempDirectory("myTempDir");
        Files.write(Paths.get(tempDirectory.toString(), "index.html"), "<!DOCTYPE html><html><body>Hello, World!</body></html>".getBytes());
        Files.write(Paths.get(tempDirectory.toString(), "test1.html"), "<!DOCTYPE html><html><body>Hello, World! TEST1</body></html>".getBytes());
        Files.write(Paths.get(tempDirectory.toString(), "test2.html"), "<!DOCTYPE html><html><body>Hello, World! TEST2</body></html>".getBytes());
    }

    @Test
    public void testFrontend() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        String string = given()
                .get("http://localhost:9999/test/index.html")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testFrontendIndex() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        String string = given()
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testFrontendNotFoundReroute() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), "/", false, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        String string = given()
                .get("http://localhost:9999/test/xxx")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testFrontend404() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        given()
                .get("http://localhost:9999/test/xxx")
                .then()
                .statusCode(404)
                .extract()
                .asString();

    }

    @Test
    public void testFrontendSecured() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, true, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        given()
                .redirects().follow(false)
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(302)
                .header("Location", "/login.html");
    }

    @Test
    public void testFrontendSecuredAllow() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, true, false, null, List.of(), List.of(
                "/xxx",
                "/test.*"
        ));

        registerServer(frontendHandler);

        given()
                .get("http://localhost:9999/test/xxx")
                .then()
                .statusCode(404);

        given()
                .get("http://localhost:9999/test/test1.html")
                .then()
                .statusCode(200);

        given()
                .get("http://localhost:9999/test/test2.html")
                .then()
                .statusCode(200);

        given()
                .get("http://localhost:9999/test/test3.html")
                .then()
                .statusCode(404);
    }

    @Test
    public void testFrontendSecuredLikeAjax() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, true, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        given()
                .header("X-Requested-With", "XMLHttpRequest")
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(401);
    }

    @Test
    public void testFrontendCompression() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of(), List.of());

        registerServer(frontendHandler);

        String string = given()
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(200)
                .header("content-encoding", "gzip")
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testFrontendNoCache() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of("/"), List.of());

        registerServer(frontendHandler);

        String string = given()
                .get("http://localhost:9999/test/")
                .then()
                .statusCode(200)
                .header("cache-control", "no-cache, no-store")
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    @Test
    public void testFrontendNoCacheRegex() {
        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of("/", "/test.*"), List.of());

        registerServer(frontendHandler);

        String string = given()
                .get("http://localhost:9999/test/test1.html")
                .then()
                .statusCode(200)
                .header("cache-control", "no-cache, no-store")
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    private void registerServer(FrontendHandler frontendHandler) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        gatewayServer.register(buildConfig(frontendHandler))
                .onComplete(event -> completableFuture.complete(event.succeeded()));

        Boolean registerSuccess;
        try {
            registerSuccess = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(registerSuccess);
    }

    private GatewayConfig buildConfig(FrontendHandler frontendHandler) {
        return new GatewayConfig(
                9999,
                "/",
                new GatewayConfig.SslConfig(false, null, null),
                new GatewayConfig.LoginConfig("/login", "/login.html", null),
                new GatewayConfig.JwtConfig(false, false, null),
                new GatewayConfig.SessionConfig(false, 1),
                List.of(),
                List.of(
                        frontendHandler
                ),
                List.of()
        );
    }

}
