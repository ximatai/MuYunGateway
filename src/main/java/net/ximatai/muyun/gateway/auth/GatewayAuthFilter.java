package net.ximatai.muyun.gateway.auth;

import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.gateway.GatewayServer;
import net.ximatai.muyun.gateway.config.Management;

import java.util.Objects;

@Provider
@ApplicationScoped
public class GatewayAuthFilter implements ContainerRequestFilter {

    @Inject
    Management management;

    @Inject
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = requestContext.getHeaderString("token");
        String clientIp = request.remoteAddress().hostAddress();

        // 验证 IP 地址
        if (!management.allowedIps().contains("*") && !management.allowedIps().contains(clientIp)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Unauthorized IP").build());
            return;
        }

        if (management.checkToken() && !Objects.equals(token, GatewayServer.token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid token").build());
            return;
        }

        // 设置自定义 SecurityContext
        SecurityContext customContext = new SimpleSecurityContext("admin", "admin", requestContext.getSecurityContext().isSecure());
        requestContext.setSecurityContext(customContext);
    }
}
