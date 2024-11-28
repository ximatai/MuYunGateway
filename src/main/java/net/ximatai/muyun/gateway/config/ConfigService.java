package net.ximatai.muyun.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.gateway.GatewayServer;
import net.ximatai.muyun.gateway.config.model.GatewayConfig;
import net.ximatai.muyun.gateway.config.model.GatewayConfigDto;
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
    GatewayConfig gatewayConfig;

    @Inject
    GatewayServer gatewayServer;

    @ConfigProperty(name = "quarkus.config.locations")
    String configFilePath;

    @PostConstruct
    void init() throws JsonProcessingException {
        logger.info("Loading config file: {}", configFilePath);
        logger.info("Config content is:\n {}", objectMapper.writeValueAsString(loadConfig()));
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
    public GatewayConfigDto loadConfig() {
        // 使用 GatewayConfig 生成 DTO
        return new GatewayConfigDto(gatewayConfig);
    }

    /**
     * 更新内存中的配置并同步到文件
     */
    public synchronized void updateConfig(GatewayConfigDto newConfig) throws IOException {
        gatewayServer.register(newConfig);
        writeConfigToFile(newConfig);
    }

    /**
     * 写入配置到文件
     */
    private void writeConfigToFile(GatewayConfigDto config) throws IOException {
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
