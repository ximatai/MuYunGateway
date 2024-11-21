package net.ximatai.muyun.gateway.config;

import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomHttpServerOptions implements HttpServerOptionsCustomizer {

    @Override
    public void customizeHttpServer(HttpServerOptions options) {
        options.setCompressionLevel(9);
        options.setCompressionSupported(true);
    }
}
