package net.ximatai.muyun.gateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TestSecurity {

    @Test
    public void testSecurityBad() {
        given()
                .get("/gw/api/config")
                .then()
                .statusCode(401);

    }

    @Test
    public void testSecurityOk() {
        String token = GatewayServer.token;
        given()
                .header("token", token)
                .get("/gw/api/config")
                .then()
                .statusCode(200);
    }

}
