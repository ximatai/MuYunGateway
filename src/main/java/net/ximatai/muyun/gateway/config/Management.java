package net.ximatai.muyun.gateway.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "management")
public interface Management {
    boolean checkToken();
    List<String> allowedIps();
}
