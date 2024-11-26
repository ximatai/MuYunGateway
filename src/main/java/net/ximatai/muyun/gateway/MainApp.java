package net.ximatai.muyun.gateway;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.config.ConfigService;
import net.ximatai.muyun.gateway.config.model.GatewayConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class MainApp implements QuarkusApplication {

    private final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Inject
    ConfigService configService;

    void init(@Observes StartupEvent startupEvent, Vertx vertx) {
        GatewayConfigDto config = configService.loadConfig();
        int port = config.getPort();

        Router router = Router.router(vertx);

        new RouteRegister(router).register(config);

        HttpServer server = vertx.createHttpServer(getServerOptions(config));
        server.requestHandler(router).listen(port)
                .onSuccess(it -> {
                    logger.info("MuYunGateway running on {}", port);
                })
                .onFailure(event -> {
                    logger.error("MuYunGateway startup failed", event);
                });

    }

    private HttpServerOptions getServerOptions(GatewayConfigDto config) {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setMaxHeaderSize(64 * 1024);
        serverOptions.setCompressionSupported(true);

        GatewayConfigDto.SslConfig sslConfig = config.getSsl();
        if (sslConfig.isUse()) {
            String certPath = sslConfig.getCertPath();
            String keyPath = sslConfig.getKeyPath();
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions().setCertPath(certPath).setKeyPath(keyPath);
            serverOptions.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }
        return serverOptions;
    }

    @Override
    public int run(String... args) {
        logger.info("Starting up...");
        Quarkus.waitForExit();
        return 0;
    }
}
