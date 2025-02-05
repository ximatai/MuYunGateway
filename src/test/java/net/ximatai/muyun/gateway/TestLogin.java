package net.ximatai.muyun.gateway;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.handler.Backend;
import net.ximatai.muyun.gateway.handler.UpstreamHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class TestLogin {

    private int loginServerPort;
    private int upstreamServerPort;

    @Inject
    GatewayServer gatewayServer;

    public TestLogin() {
        mockLoginServer();
        mockUpstreamServer();
    }

    @Test
    @DisplayName("测试登录接口可用")
    public void testLoginServer() {
        Map user = given()
                .contentType("application/json")
                .body(Map.of("username", "admin", "password", "admin"))
                .when()
                .post("http://127.0.0.1:%s/api/login".formatted(loginServerPort))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals(1, user.get("status"));
        assertEquals("admin", user.get("user"));

    }

    @Test
    @DisplayName("测试登录成功后用session和jwt验证身份")
    public void testUpstreamServer() {
        registerServer();

        // 未登录状态下访问 /api/whoami
        given()
                .redirects().follow(false)
                .get("http://127.0.0.1:9999/api/whoami")
                .then()
                .statusCode(302)
                .header("Location", "/login.html");

        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", "admin", "password", "admin"))
                .when()
                .post("http://127.0.0.1:9999/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String myCookie = response.getCookie("muyun-gateway");

        assertNotNull(myCookie);

        given()
                .cookie("muyun-gateway", myCookie)
                .get("http://127.0.0.1:9999/api/whoami")
                .then()
                .statusCode(200);

        String authorization = response.getHeader("authorization");

        assertNotNull(authorization);

        given()
                .header("Authorization", authorization)
                .get("http://127.0.0.1:9999/api/whoami")
                .then()
                .statusCode(200);

        Map user = given()
                .cookie("muyun-gateway", myCookie + "x")
                .header("Authorization", authorization)
                .get("http://127.0.0.1:9999/api/whoami")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(1, user.get("status"));
    }

    private void mockLoginServer() {
        boolean serverStarted = false;
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        router.post("/api/login")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    JsonObject user = routingContext.body().asJsonObject();
                    String username = user.getString("username");
                    String password = user.getString("password");
                    if (username.equals("admin") && password.equals("admin")) {
                        response
                                .putHeader("content-type", "application/json")
                                .end(new JsonObject().put("user", username).put("status", 1).encode());
                    } else {
                        response
                                .setStatusCode(500)
                                .end("Login failed");
                    }
                });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0)
                .onComplete(event -> {
                    loginServerPort = event.result().actualPort();
                    completableFuture.complete(event.succeeded());
                });

        try {
            serverStarted = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(serverStarted);
    }

    private void mockUpstreamServer() {
        boolean serverStarted = false;
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        router.get("/api/whoami")
                .handler(routingContext -> {
                    HttpServerRequest request = routingContext.request();
                    HttpServerResponse response = routingContext.response();
                    String gwUser = request.getHeader("gw-user");
                    JsonObject user = new JsonObject(new String(Base64.getDecoder().decode(gwUser)));
                    response.putHeader("content-type", "application/json")
                            .end(user.encodePrettily());
                });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0)
                .onComplete(event -> {
                    upstreamServerPort = event.result().actualPort();
                    completableFuture.complete(event.succeeded());
                });

        try {
            serverStarted = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(serverStarted);
    }

    private void registerServer() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        gatewayServer.register(
                        new GatewayConfig(
                                9999,
                                "/",
                                new GatewayConfig.SslConfig(false, null, null),
                                new GatewayConfig.LoginConfig("/login", "/login.html", "http://127.0.0.1:%s/api/login".formatted(loginServerPort)),
                                new GatewayConfig.JwtConfig(true, false, 60),
                                new GatewayConfig.SessionConfig(true, 1),
                                List.of(),
                                List.of(),
                                List.of(
                                        new UpstreamHandler("/api", true, false, "", List.of(), List.of(), List.of(
                                                new Backend("http://localhost:%s/api/".formatted(upstreamServerPort), 1)
                                        ))
                                )
                        )
                )
                .onComplete(event -> completableFuture.complete(event.succeeded()));

        Boolean registerSuccess;
        try {
            registerSuccess = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(registerSuccess);
    }
}
