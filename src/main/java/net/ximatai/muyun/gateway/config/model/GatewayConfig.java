package net.ximatai.muyun.gateway.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import net.ximatai.muyun.gateway.handler.Backend;
import net.ximatai.muyun.gateway.handler.FrontendHandler;
import net.ximatai.muyun.gateway.handler.UpstreamHandler;

import java.io.Serializable;
import java.util.List;

public class GatewayConfig implements Serializable {

    private final int port;
    private final String index;
    private final SslConfig ssl;
    private final LoginConfig login;
    private final JwtConfig jwt;
    private final SessionConfig session;
    private final List<Redirect> redirects;
    private final List<String> whiteReferer;
    private final List<FrontendHandler> frontends;
    private final List<UpstreamHandler> upstreams;

    @JsonCreator
    public GatewayConfig(
            int port,
            String index,
            SslConfig ssl,
            LoginConfig login,
            JwtConfig jwt,
            SessionConfig session,
            List<Redirect> redirects,
            List<String> whiteReferer,
            List<FrontendHandler> frontends,
            List<UpstreamHandler> upstreams
    ) {
        this.port = port;
        this.index = index;
        this.ssl = ssl;
        this.login = login;
        this.jwt = jwt;
        this.session = session;
        this.redirects = redirects;
        this.whiteReferer = whiteReferer;
        this.frontends = frontends;
        this.upstreams = upstreams;
    }

    // 从 GatewayConfig 转换的构造函数
    public GatewayConfig(IGatewayConfig IGatewayConfig) {
        this.port = IGatewayConfig.port();
        this.index = IGatewayConfig.index();
        this.ssl = new SslConfig(
                IGatewayConfig.ssl().use().orElse(false),
                IGatewayConfig.ssl().certPath().orElse(null),
                IGatewayConfig.ssl().keyPath().orElse(null)
        );
        this.login = new LoginConfig(
                IGatewayConfig.login().path().orElse("/login"),
                IGatewayConfig.login().page(),
                IGatewayConfig.login().api().orElse(null)
        );
        this.jwt = new JwtConfig(
                IGatewayConfig.jwt().use(),
                IGatewayConfig.jwt().checkExpiration(),
                IGatewayConfig.jwt().expiresMin().orElse(null)
        );
        this.session = new SessionConfig(
                IGatewayConfig.session().use().orElse(true),
                IGatewayConfig.session().timeoutHour().orElse(24)
        );
        this.redirects = IGatewayConfig.redirects().stream()
                .map(r -> new Redirect(r.from(), r.to()))
                .toList();
        this.whiteReferer = IGatewayConfig.whiteReferer().orElse(List.of());
        this.frontends = IGatewayConfig.frontends().stream()
                .map(f -> new FrontendHandler(
                        f.path(),
                        f.dir(),
                        f.notFoundReroute().orElse(null),
                        f.secured().orElse(false),
                        f.regex().orElse(false),
                        f.comment().orElse(null),
                        f.noCache().orElse(List.of()),
                        f.allowlist().orElse(List.of())
                )).toList();
        this.upstreams = IGatewayConfig.upstreams().stream()
                .map(u -> new UpstreamHandler(
                        u.path(),
                        u.secured().orElse(false),
                        u.regex().orElse(false),
                        u.comment().orElse(null),
                        u.noCache().orElse(List.of()),
                        u.allowlist().orElse(List.of()),
                        u.backends().stream().map(backend -> new Backend(backend.url(), backend.weight().orElse(1))).toList()
                )).toList();
    }

    // Getters
    public int getPort() {
        return port;
    }

    public String getIndex() {
        return index;
    }

    public SslConfig getSsl() {
        return ssl;
    }

    public LoginConfig getLogin() {
        return login;
    }

    public JwtConfig getJwt() {
        return jwt;
    }

    public SessionConfig getSession() {
        return session;
    }

    public List<Redirect> getRedirects() {
        return redirects;
    }

    public List<String> getWhiteReferer() {
        return whiteReferer;
    }

    public List<FrontendHandler> getFrontends() {
        return frontends;
    }

    public List<UpstreamHandler> getUpstreams() {
        return upstreams;
    }

    // 嵌套类定义
    public record SslConfig(boolean use, String certPath, String keyPath) {
    }

    public record LoginConfig(String path, String page, String api) {
    }

    public record JwtConfig(boolean use, boolean checkExpiration, Integer expiresMin) {
    }

    public record SessionConfig(boolean use, Integer timeoutHour) {
    }

    public record Redirect(String from, String to) {
    }

}
