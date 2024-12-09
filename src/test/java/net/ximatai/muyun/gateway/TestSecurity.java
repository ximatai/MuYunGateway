package net.ximatai.muyun.gateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TestSecurity {

    @Test
    @DisplayName("测试未授权访问 /api/config 返回 401 状态码")
    public void testSecurityBad() {
        given()
                .get("/api/config")
                .then()
                .statusCode(401);

    }

    @Test
    @DisplayName("测试带有有效 token 的访问 /api/config 返回 200 状态码")
    public void testSecurityOk() {
        String token = GatewayServer.token;
        given()
                .header("token", token)
                .get("/api/config")
                .then()
                .statusCode(200);
    }

}
