package net.ximatai.muyun.gateway;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.handler.FrontendHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("测试前端访问 index.html 文件")
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
    @DisplayName("测试前端根路径访问")
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
    @DisplayName("测试前端未找到资源时重定向到默认页面")
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
    @DisplayName("测试前端未找到资源时返回 404 状态码")
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
    @DisplayName("测试前端安全模式下访问根路径重定向到登录页面")
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
    @DisplayName("测试前端安全模式下允许特定路径访问")
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
    @DisplayName("测试前端安全模式下 AJAX 请求返回 401 状态码")
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
    @DisplayName("测试前端响应压缩")
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
    @DisplayName("测试前端响应不缓存根路径")
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
    @DisplayName("测试前端响应不缓存特定路径")
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
