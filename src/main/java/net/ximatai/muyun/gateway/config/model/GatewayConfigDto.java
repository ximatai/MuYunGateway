package net.ximatai.muyun.gateway.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import net.ximatai.muyun.gateway.handler.FrontendHandler;

import java.io.Serializable;
import java.util.List;

public class GatewayConfigDto implements Serializable {

    private final int port;
    private final String index;
    private final SslConfig ssl;
    private final LoginConfig login;
    private final JwtConfig jwt;
    private final SessionConfig session;
    private final List<Redirect> redirects;
    private final List<String> whiteReferer;
    private final List<FrontendHandler> frontends;

    @JsonCreator
    public GatewayConfigDto(
            int port,
            String index,
            SslConfig ssl,
            LoginConfig login,
            JwtConfig jwt,
            SessionConfig session,
            List<Redirect> redirects,
            List<String> whiteReferer,
            List<FrontendHandler> frontends
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
    }

    // 从 GatewayConfig 转换的构造函数
    public GatewayConfigDto(GatewayConfig gatewayConfig) {
        this.port = gatewayConfig.port();
        this.index = gatewayConfig.index();
        this.ssl = new SslConfig(
                gatewayConfig.ssl().use(),
                gatewayConfig.ssl().certPath().orElse(null),
                gatewayConfig.ssl().keyPath().orElse(null)
        );
        this.login = new LoginConfig(
                gatewayConfig.login().page(),
                gatewayConfig.login().api().orElse(null)
        );
        this.jwt = new JwtConfig(
                gatewayConfig.jwt().use(),
                gatewayConfig.jwt().checkExpiration(),
                gatewayConfig.jwt().expiresMin().orElse(null)
        );
        this.session = new SessionConfig(
                gatewayConfig.session().use(),
                gatewayConfig.session().timeoutHour().orElse(null)
        );
        this.redirects = gatewayConfig.redirects().stream()
                .map(r -> new Redirect(r.from(), r.to()))
                .toList();
        this.whiteReferer = gatewayConfig.whiteReferer().orElse(List.of());
        this.frontends = gatewayConfig.frontends().stream()
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

    // 嵌套类定义
    public record SslConfig(boolean use, String certPath, String keyPath) {
    }

    public record LoginConfig(String page, String api) {
    }

    public record JwtConfig(boolean use, boolean checkExpiration, Integer expiresMin) {
    }

    public record SessionConfig(boolean use, Integer timeoutHour) {
    }

    public record Redirect(String from, String to) {
    }

}
