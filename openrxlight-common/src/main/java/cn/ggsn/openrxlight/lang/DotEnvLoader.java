package cn.ggsn.openrxlight.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import com.google.common.collect.Maps;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * 加载项目根目录下的 .env 文件并将属性注入到系统环境变量中
 * 仅在 dev 环境下启用，生产环境不加载 .env 文件
 * 在 Quarkus 应用启动时自动执行
 */
@Slf4j
@ApplicationScoped
@Startup
@IfBuildProfile("dev")
public class DotEnvLoader {

    private static final Map<String, String> LOADED_PROPERTIES = Maps.newHashMap();

    @PostConstruct
    public void loadDotEnv() {
        log.info("Loading DotEnvLoader and initializing properties");
        try {
            // 尝试从多个可能的位置加载 .env 文件
            Path[] possiblePaths = {
                    // 当前工作目录
                    Paths.get(System.getProperty("user.dir"), ".env"),
                    // 类路径根目录
                    Paths.get(System.getProperty("user.dir"), "openrxlight-api", ".env"),
                    Paths.get(System.getProperty("user.dir"), "rxlight", ".env"),
                    // 目标 classes 目录
                    Paths.get(System.getProperty("user.dir"), "target", "classes", ".env"),
                    Paths.get(System.getProperty("user.dir"), "rxlight", "target", "classes", ".env"),
                    Paths.get(System.getProperty("user.dir"), "openrxlight-api", "target", "classes", ".env"),
            };

            for (Path path : possiblePaths) {
                if (Files.exists(path)) {
                    log.info("Loading .env file from: {}", path.toAbsolutePath());
                    loadEnvFile(path);
                    break;
                }
            }

            // 将加载的属性注入到系统环境变量
            injectIntoSystemEnv();
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}", e.getMessage());
        }
    }

    /**
     * 加载 .env 文件
     */
    private void loadEnvFile(Path path) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // 解析 key=value
                int delimiterIndex = line.indexOf('=');
                if (delimiterIndex <= 0) {
                    log.warn("Skipping invalid line {}: {}", lineNum, line);
                    continue;
                }

                String key = line.substring(0, delimiterIndex).trim();
                String value = line.substring(delimiterIndex + 1).trim();

                // 处理引号
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                LOADED_PROPERTIES.put(key, value);
                log.debug("Loaded env property: {}={}", maskKey(key), maskValue(value));
            }
        }
    }

    /**
     * 将加载的属性注入到系统环境变量
     * 注意：Java 不允许直接修改 System.getenv()，但可以通过反射修改 ProcessEnvironment
     */
    private void injectIntoSystemEnv() {
        for (Map.Entry<String, String> entry : LOADED_PROPERTIES.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 设置为系统属性（Quarkus 可以读取）
            System.setProperty(key, value);
        }

        log.info("Injected {} properties into system environment", LOADED_PROPERTIES.size());
    }

    /**
     * 获取已加载的属性值
     */
    public static String getProperty(String key) {
        return LOADED_PROPERTIES.get(key);
    }

    /**
     * 获取所有已加载的属性
     */
    public static Map<String, String> getAllProperties() {
        return Map.copyOf(LOADED_PROPERTIES);
    }

    /**
     * 脱敏显示 key（用于日志）
     */
    private String maskKey(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        return key;
    }

    /**
     * 脱敏显示 value（用于日志，针对敏感信息）
     */
    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String lowerValue = value.toLowerCase();
        // 对敏感信息进行脱敏
        if (lowerValue.contains("secret") || lowerValue.contains("key") || lowerValue.contains("password")
                || lowerValue.contains("token")) {
            return value.substring(0, Math.min(3, value.length())) + "***";
        }
        return value;
    }

    @PreDestroy
    public void destroy() {
        log.info("Destroying DotEnvLoader and clearing loaded properties");
        LOADED_PROPERTIES.clear();
    }
}
