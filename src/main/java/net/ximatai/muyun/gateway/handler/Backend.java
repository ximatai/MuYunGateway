package net.ximatai.muyun.gateway.handler;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@JsonIncludeProperties({"url", "weight"})
public class Backend {
    private final String url;
    private final int weight;
    private HttpClient client;
    private String path;
    private int port;
    private String host;
    private Vertx vertx;
    private boolean isSSL;

    private boolean online = true;

    public Backend(String url, Integer weight) {
        this.url = url = url.trim();
        this.weight = weight;

        try {
            URI uri = new URI(url);
            isSSL = uri.toURL().getProtocol().equals("https");
            path = uri.getPath();
            port = uri.getPort();
            if (port == -1) {
                port = isSSL ? 443 : 80;
            }

            host = uri.getHost();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    String path() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isOnline() {
        return online;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    HttpClient getClient(Vertx vertx) {
        this.vertx = vertx;
        if (client == null) {
            var option = new HttpClientOptions();
            option.setDefaultHost(host);
            option.setDefaultPort(port);

            option.setSsl(isSSL);
            option.setKeepAlive(true);

            option.setDecompressionSupported(true);
            option.setMaxPoolSize(100);

            option.setIdleTimeout(150_000);
            option.setKeepAliveTimeout(60);
            option.setConnectTimeout(60_000);

            if (isSSL) {
                option.setTrustAll(true);
            }

            client = vertx.createHttpClient(option);
            online = true;
        }

        return client;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Backend) obj;
        return Objects.equals(this.url, that.url) &&
                this.weight == that.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, weight);
    }

    @Override
    public String toString() {
        return "Backend[" +
                "url=" + url + ", " +
                "weight=" + weight + ']';
    }

}
