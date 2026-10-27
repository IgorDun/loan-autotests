package dev.kozhevnikov.autotests.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import dev.kozhevnikov.autotests.api.model.DatabaseConfig;

import java.util.List;
import java.util.Map;
/**
*
* Конфигурационный класс для подключения к базе данных.
 Содержит информацию о доступных источниках данных (поле datasources) и стандартной конфигурации базы данных (поле defaultConfig).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "db")
@PropertySource(value = "classpath:database.properties", encoding = "UTF-8")
public class DatabaseConnectionConfiguration {
    private List<DatabaseConfig> datasources;
    private DbDefaultConfig defaultConfig;

    /**
     * Класс, представляющий стандартную конфигурацию базы данных.
     * Содержит информацию о паролях (passwords) и портах (port) для подключения к базе данных
     */
    @Data
    public static class DbDefaultConfig {
        private Map<String, String> passwords;
        private Map<String, String> port;
    }
}