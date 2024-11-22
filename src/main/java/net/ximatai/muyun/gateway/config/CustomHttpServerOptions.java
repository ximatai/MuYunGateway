package net.ximatai.muyun.gateway.config;

import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CustomHttpServerOptions implements HttpServerOptionsCustomizer {

    @Override
    public void customizeHttpServer(HttpServerOptions options) {
        options.setCompressionSupported(true);
        options.setCompressors(List.of(StandardCompressionOptions.gzip()));
    }
}
