package net.ximatai.muyun.gateway;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class MainApp implements QuarkusApplication {

    private final Logger logger = LoggerFactory.getLogger(MainApp.class);

    void init(@Observes StartupEvent startupEvent, Router router) {
        new RouteRegister(router);
    }

    @Override
    public int run(String... args) {
        logger.info("Starting up...");
        Quarkus.waitForExit();
        return 0;
    }
}
