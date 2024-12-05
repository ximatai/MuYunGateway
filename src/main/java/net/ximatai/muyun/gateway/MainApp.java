package net.ximatai.muyun.gateway;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.ConfigService;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;

@QuarkusMain
public class MainApp implements QuarkusApplication {

    @Inject
    ConfigService configService;

    @Inject
    GatewayServer gatewayServer;

    void init(@Observes StartupEvent startupEvent) {
        GatewayConfig config = configService.loadConfig();
        gatewayServer.register(config);
    }

    @Override
    public int run(String... args) {
        Quarkus.waitForExit();
        return 0;
    }
}
