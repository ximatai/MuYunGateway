package net.ximatai.muyun.gateway.config.model;

import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "gateway")
public interface GatewayConfig {

    int port();

    /**
     * 网关的默认首页路径
     */
    String index();

    /**
     * SSL 配置
     */
    SslConfig ssl();

    /**
     * 登录配置
     */
    LoginConfig login();

    /**
     * JWT 配置
     */
    JwtConfig jwt();

    /**
     * 会话配置
     */
    SessionConfig session();

    /**
     * 重定向规则列表
     */
    List<Redirect> redirects();

    /**
     * Referer 白名单
     */
    Optional<List<String>> whiteReferer();

    /**
     * 前端路径配置
     */
    List<Frontend> frontends();

    /**
     * 后端服务代理
     */
    List<Upstream> upstreams();

    // ---- 嵌套配置定义 ----

    interface SslConfig {
        boolean use();

        Optional<String> certPath();

        Optional<String> keyPath();
    }

    interface LoginConfig {
        String page();

        Optional<String> path();

        Optional<String> api();
    }

    interface JwtConfig {
        boolean use();

        boolean checkExpiration();

        Optional<Integer> expiresMin();
    }

    interface SessionConfig {
        boolean use();

        Optional<Integer> timeoutHour();
    }

    interface Redirect {
        String from();

        String to();
    }

    interface Frontend {
        String path();

        String dir();

        Optional<String> notFoundReroute();

        Optional<Boolean> secured();

        Optional<Boolean> regex();

        Optional<String> comment();

        Optional<List<String>> noCache();

        Optional<List<String>> allowlist();
    }

    interface Upstream {
        String path();

        Optional<Boolean> secured();

        Optional<Boolean> regex();

        Optional<String> comment();

        Optional<List<String>> noCache();

        Optional<List<String>> allowlist();

        List<Backend> backends();
    }

    interface Backend {
        String url();

        Optional<Integer> weight();
    }
}
