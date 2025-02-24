package net.ximatai.muyun.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.runtime.Startup;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.GatewayServer;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.config.model.IGatewayConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Startup
@ApplicationScoped
public class ConfigService {

    private final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private final ObjectMapper objectMapper;

    @Inject
    IGatewayConfig IGatewayConfig;

    @Inject
    GatewayServer gatewayServer;

    private GatewayConfig gatewayConfig;

    @ConfigProperty(name = "quarkus.config.locations")
    String configFilePath;

    @PostConstruct
    void init() throws JsonProcessingException {
        logger.info("Loading config file: {}", configFilePath);
    }

    @Inject
    public ConfigService() {
        this.objectMapper = new ObjectMapper(new YAMLFactory())
                .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    }

    /**
     * 获取配置文件路径
     */
    public Path getConfigFile() {
        return Path.of(configFilePath);
    }

    /**
     * 加载当前配置文件内容到内存
     */
    public GatewayConfig loadConfig() {
        if (gatewayConfig == null) {
            gatewayConfig = new GatewayConfig(IGatewayConfig); // 使用 GatewayConfig 生成 DTO
        }
        return gatewayConfig;
    }

    /**
     * 更新内存中的配置并同步到文件
     */
    public Future<Integer> updateConfig(GatewayConfig newConfig) {
        Promise<Integer> promise = Promise.promise();
        gatewayServer.register(newConfig)
                .onSuccess(result -> {
                    try {
                        writeConfigToFile(newConfig);
                        gatewayConfig = newConfig;
                        promise.complete(result);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                        promise.fail(e);
                    }
                })
                .onFailure(promise::fail);

        return promise.future();
    }

    /**
     * 写入配置到文件
     */
    private void writeConfigToFile(GatewayConfig config) throws IOException {
        Path path = getConfigFile();

        try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // 包装配置对象，添加根节点 "gateway"
            objectMapper.writeValue(writer, Map.of("gateway", config));

            logger.info("配置已成功写入文件: {}", path);
        } catch (Exception e) {
            throw new IOException("写入配置文件失败: " + e.getMessage(), e);
        }
    }
}
