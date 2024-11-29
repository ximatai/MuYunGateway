package net.ximatai.muyun.gateway.endpoint;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.ximatai.muyun.gateway.config.ConfigService;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

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
    public GatewayConfig getConfig() {
        return configService.loadConfig();
    }

    /**
     * 更新配置
     */
    @POST
    public boolean updateConfig(GatewayConfig newConfig) {
        try {
            configService.updateConfig(newConfig);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
