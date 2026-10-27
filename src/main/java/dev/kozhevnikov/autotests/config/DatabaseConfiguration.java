package dev.kozhevnikov.autotests.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import dev.kozhevnikov.autotests.api.model.DatabaseConfig;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Конфигурационный класс для работы с базой данных.
 */

@Slf4j
@EqualsAndHashCode
@Setter
@SuppressWarnings("unused")
@RequiredArgsConstructor
@Configuration
public class DatabaseConfiguration {
    private final DatabaseConnectionConfiguration databaseConnectionConfiguration;

    /**
     * Пример бина JdbcTemplate для конкретного сервиса. Остальные базы доступны через {@link #getDb(String, String)}.
     *
     * @param envSettings настройки окружения
     * @return JdbcTemplate базы сервиса customer
     */
    @Bean
    @Lazy
    public JdbcTemplate customerDb(EnvSettings envSettings) {
        return getDb("customer", envSettings.getInventoryName());
    }

    /**
     * Получает объект JdbcTemplate для указанного сервиса из списка доступных баз данных.
     * @param serviceName название сервиса для получения базы данных
     * @param inventoryName название переменой для создания DataSource
     * @return JdbcTemplate для доступа к базе данных сервиса
     * @throws RuntimeException если база данных для указанного сервиса не найдена
     */
    public JdbcTemplate getDb(String serviceName, String inventoryName) {
        return databaseConnectionConfiguration.getDatasources().stream()
                .filter(database -> database.getServiceName().equals(serviceName))
                .findFirst()
                .map(databaseConfig -> createDataSource(databaseConfig, inventoryName))
                .map(dataSource -> new JdbcTemplate(dataSource, true))
                .orElseThrow(() -> new RuntimeException("Service with name '" + serviceName + "' not found"));
    }

    private DataSource createDataSource(DatabaseConfig databaseConfig, String inventoryName) {
        String enrichedUrl;
        String user;
        String passwordDb = Optional.ofNullable(databaseConfig.getPasswords())
                .map(passwords -> passwords.get(inventoryName))
                .orElseGet(() -> databaseConnectionConfiguration.getDefaultConfig().getPasswords().get(inventoryName));

        String portDb = Optional.ofNullable(databaseConfig.getPort())
                .map(port -> port.get(inventoryName))
                .orElseGet(() -> databaseConnectionConfiguration.getDefaultConfig().getPort().get(inventoryName));
        enrichedUrl = databaseConfig.getUrl()
                .replace("${env}", inventoryName)
                .replace("${port}", String.valueOf(portDb));
        user = databaseConfig.getUser();

        DataSource source = DataSourceBuilder
                .create()
                .url(enrichedUrl)
                .username(user)
                .password(passwordDb)
                .type(HikariDataSource.class)
                .build();
        log.info(String.format("БД: %s - инициализирована", databaseConfig));

        return source;
    }
}