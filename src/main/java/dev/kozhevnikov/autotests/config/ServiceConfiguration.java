package dev.kozhevnikov.autotests.config;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;

import java.util.HashMap;
import java.util.List;

/**
 * Конфигурационный класс для настроек сервисов.
 * Этот класс содержит информацию о следующих компонентах:
 * <ul>
 * <li>defaultService - сервис по умолчанию;</li>
 * <li>services - список доступных сервисов;</li>
 * <li>envSettings - настройки окружения;</li>
 * </ul>
 */

@Configuration
@ConfigurationProperties(prefix = "api")
@PropertySource(value = "classpath:services.properties", encoding = "UTF-8")
@Setter
@EqualsAndHashCode
@SuppressWarnings("unused")
public class ServiceConfiguration {

    private ServiceConfig defaultService;
    private List<ServiceConfig> services;
    private EnvSettings envSettings;

    /**
     * Создает список ServiceConfig с учетом окружения.
     * @param envSettings объект EnvSettings, содержащий информацию о настройках окружения
     * @return список ServiceConfig с обработанными данными о портах, таймаутах, заголовках и URL
     */
    @Bean
    @Lazy
    public List<ServiceConfig> serviceList(EnvSettings envSettings) {
        services.forEach(service -> {
            if (service.getPort() == null) {
                service.setPort(defaultService.getPort());
            }
            if (service.getTimeout() == null) {
                service.setTimeout(defaultService.getTimeout());
            }
            if (service.getHeaders() == null) {
                service.setHeaders(new HashMap<>());
            }
            defaultService.getHeaders().forEach((key, value) -> service.getHeaders().putIfAbsent(key, value));
            String enrichedUrl = service.getUrl().replace("${inventoryName}", envSettings.getInventoryName());
            service.setUrl(enrichedUrl);
        });

        return services;
    }

    /**
     * Получает объект ServiceConfig по имени сервиса из списка доступных сервисов.
     * @param serviceName название сервиса, для которого нужно получить объект ServiceConfig
     * @return объект ServiceConfig с указанным именем
     */
    public ServiceConfig getService(String serviceName) {
        return serviceList(envSettings).stream()
                .filter(service -> service.getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service with name '" + serviceName + "' not found"));
    }

    /**
     * Бин на каждый часто используемый сервис. Остальные сервисы доступны по имени через {@link #getService(String)}.
     */
    @Bean
    public ServiceConfig settingsService() {
        return getService("settings");
    }

    @Bean
    public ServiceConfig wiremockService() {
        return getService("wiremock");
    }
}
