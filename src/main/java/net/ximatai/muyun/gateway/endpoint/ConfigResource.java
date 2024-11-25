package net.ximatai.muyun.gateway.endpoint;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.gateway.config.ConfigService;
import net.ximatai.muyun.gateway.config.model.GatewayConfigDto;

import java.io.IOException;

@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    @Inject
    ConfigService configService;

    /**
     * 获取当前配置
     */
    @GET
    public GatewayConfigDto getConfig() {
        return configService.loadConfig();
    }

    /**
     * 更新配置
     */
    @POST
    public Response updateConfig(GatewayConfigDto newConfig) {
        try {
            configService.updateConfig(newConfig);
            return Response.ok("配置更新成功").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("配置无效: " + e.getMessage())
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("更新配置失败: " + e.getMessage())
                    .build();
        }
    }
}
