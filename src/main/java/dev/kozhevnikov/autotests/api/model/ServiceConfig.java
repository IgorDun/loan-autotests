package dev.kozhevnikov.autotests.api.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Описывает сервис с указанными компонентами:
 * <ul>
 * <li>name - название сервиса;</li>
 * <li>port  - используемый порт;</li>
 * <li>headers - заголовки для запросов к сервису;</li>
 * <li>uri - uri сервиса;</li>
 * <li>timeout - время ожидания ответа сервиса;</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ServiceConfig {
    private String name;
    private Integer port;
    private Map<String, String> headers;
    private String url;
    private Long timeout;
}
