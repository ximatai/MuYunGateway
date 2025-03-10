package net.ximatai.muyun.gateway.config.model;

import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "gateway")
public interface IGatewayConfig {

    int port();

    /**
     * 网关的默认首页路径
     */
    Optional<String> index();

    /**
     * SSL 配置
     */
    ISslConfig ssl();

    /**
     * 登录配置
     */
    ILoginConfig login();

    /**
     * JWT 配置
     */
    IJwtConfig jwt();

    /**
     * 会话配置
     */
    ISessionConfig session();

    /**
     * 重定向规则列表
     */
    List<IRedirect> redirects();

    /**
     * 附加 header 设置
     */
    Optional< List<IHeader>> headers();


    /**
     * 前端路径配置
     */
    List<IFrontend> frontends();

    /**
     * 后端服务代理
     */
    List<IUpstream> upstreams();

    // ---- 嵌套配置定义 ----

    interface ISslConfig {
        Optional<Boolean> use();

        Optional<String> certPath();

        Optional<String> keyPath();
    }

    interface ILoginConfig {
        Optional<String> page();

        Optional<String> path();

        Optional<String> api();
    }

    interface IJwtConfig {
        boolean use();

        boolean checkExpiration();

        Optional<Integer> expiresHour();
    }

    interface ISessionConfig {
        Optional<Boolean> use();

        Optional<Integer> timeoutHour();
    }

    interface IHeader {
        String name();

        String value();
    }

    interface IRedirect {
        String from();

        String to();
    }

    interface IFrontend {
        String path();

        String dir();

        Optional<String> notFoundReroute();

        Optional<Boolean> secured();

        Optional<Boolean> regex();

        Optional<String> comment();

        Optional<List<String>> noCache();

        Optional<List<String>> allowlist();
    }

    interface IUpstream {
        String path();

        Optional<Boolean> secured();

        Optional<Boolean> regex();

        Optional<String> comment();

        Optional<List<String>> noCache();

        Optional<List<String>> allowlist();

        List<IBackend> backends();
    }

    interface IBackend {
        String url();

        Optional<Integer> weight();
    }
}
