package net.ximatai.muyun.gateway;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.ConfigService;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class MainApp implements QuarkusApplication {

    private final Logger logger = LoggerFactory.getLogger(MainApp.class);

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
        logger.info("Startup successful.");
        Quarkus.waitForExit();
        return 0;
    }
}
