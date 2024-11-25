package net.ximatai.muyun.gateway.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;

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
    private final List<Frontend> frontends;

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
            List<Frontend> frontends
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
        this.whiteReferer = gatewayConfig.whiteReferer();
        this.frontends = gatewayConfig.frontends().stream()
                .map(f -> new Frontend(
                        f.path(),
                        f.dir(),
                        f.notFoundReroute().orElse(null),
                        f.protect(),
                        f.regex(),
                        f.comment().orElse(null),
                        f.noStore(),
                        f.whiteList()
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

    public List<Frontend> getFrontends() {
        return frontends;
    }

    // 嵌套类定义
    public static class SslConfig {
        private final boolean use;
        private final String certPath;
        private final String keyPath;

        public SslConfig(boolean use, String certPath, String keyPath) {
            this.use = use;
            this.certPath = certPath;
            this.keyPath = keyPath;
        }

        public boolean isUse() {
            return use;
        }

        public String getCertPath() {
            return certPath;
        }

        public String getKeyPath() {
            return keyPath;
        }
    }

    public static class LoginConfig {
        private final String page;
        private final String api;

        public LoginConfig(String page, String api) {
            this.page = page;
            this.api = api;
        }

        public String getPage() {
            return page;
        }

        public String getApi() {
            return api;
        }
    }

    public static class JwtConfig {
        private final boolean use;
        private final boolean checkExpiration;
        private final Integer expiresMin;

        public JwtConfig(boolean use, boolean checkExpiration, Integer expiresMin) {
            this.use = use;
            this.checkExpiration = checkExpiration;
            this.expiresMin = expiresMin;
        }

        public boolean isUse() {
            return use;
        }

        public boolean isCheckExpiration() {
            return checkExpiration;
        }

        public Integer getExpiresMin() {
            return expiresMin;
        }
    }

    public static class SessionConfig {
        private final boolean use;
        private final Integer timeoutHour;

        public SessionConfig(boolean use, Integer timeoutHour) {
            this.use = use;
            this.timeoutHour = timeoutHour;
        }

        public boolean isUse() {
            return use;
        }

        public Integer getTimeoutHour() {
            return timeoutHour;
        }
    }

    public static class Redirect {
        private final String from;
        private final String to;

        public Redirect(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

    public static class Frontend {
        private final String path;
        private final String dir;
        private final String notFoundReroute;
        private final boolean protect;
        private final boolean regex;
        private final String comment;
        private final List<String> noStore;
        private final List<String> whiteList;

        public Frontend(String path, String dir, String notFoundReroute, boolean protect,
                        boolean regex, String comment, List<String> noStore, List<String> whiteList) {
            this.path = path;
            this.dir = dir;
            this.notFoundReroute = notFoundReroute;
            this.protect = protect;
            this.regex = regex;
            this.comment = comment;
            this.noStore = noStore;
            this.whiteList = whiteList;
        }

        public String getPath() {
            return path;
        }

        public String getDir() {
            return dir;
        }

        public String getNotFoundReroute() {
            return notFoundReroute;
        }

        public boolean isProtect() {
            return protect;
        }

        public boolean isRegex() {
            return regex;
        }

        public String getComment() {
            return comment;
        }

        public List<String> getNoStore() {
            return noStore;
        }

        public List<String> getWhiteList() {
            return whiteList;
        }
    }
}
