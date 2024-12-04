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

    @Inject
    GatewayServer gatewayServer;

    @Test
    public void testFrontend() throws IOException, ExecutionException, InterruptedException {
        Path tempDirectory = Files.createTempDirectory("myTempDir");
        System.out.println("临时文件夹创建成功，路径：" + tempDirectory.toAbsolutePath());

        // 在临时文件夹中创建 index.html 文件
        Path indexHtml = Paths.get(tempDirectory.toString(), "index.html");
        Files.write(indexHtml, "<!DOCTYPE html><html><body>Hello, World!</body></html>".getBytes());
        System.out.println("index.html 文件已创建，路径：" + indexHtml.toAbsolutePath());

        FrontendHandler frontendHandler = new FrontendHandler("/test", tempDirectory.toAbsolutePath().toString(), null, false, false, null, List.of(), List.of());
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        gatewayServer.register(buildConfig(frontendHandler))
                .onComplete(event -> {
                    completableFuture.complete(event.succeeded());
                });

        Boolean registerSuccess = completableFuture.get();

        Assertions.assertTrue(registerSuccess);

        String string = given()
                .get("http://localhost:9999/test/index.html")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        Assertions.assertTrue(string.contains("Hello, World!"));
    }

    GatewayConfig buildConfig(FrontendHandler frontendHandler) {
        return new GatewayConfig(
                9999,
                "/",
                new GatewayConfig.SslConfig(false, null, null),
                new GatewayConfig.LoginConfig("/login", "", null),
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
