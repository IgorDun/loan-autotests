package dev.kozhevnikov.autotests.api.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Описывает базу данных с указанными компонентами:
 * <ul>
 * <li>serviceName - имя сервиса;</li>
 * <li>driver - драйвер базы данных;</li>
 * <li>dbName - имя базы данных;</li>
 * <li>url - url базы данных, поддерживает ${env} и ${port};</li>
 * <li>user - пользователь базы данных;</li>
 * <li>passwords - пароли базы данных;</li>
 * <li>port - порт для доступа к базе данных.</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DatabaseConfig {
    private String serviceName;
    private String driver;
    private String dbName;
    private String url;
    private String user;
    private Map<String, String> passwords;
    private Map<String, String> port;
}
