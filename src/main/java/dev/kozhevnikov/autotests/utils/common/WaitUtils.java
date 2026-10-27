package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Component
@Lazy
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class WaitUtils {
    /**
     * Класс управляющий разными типами ожиданий в зависимости от потребностей автотестов.
     */
    private final ServiceUtils serviceUtils;
    private final EnvSettings envSettings;

    /**
     * Возвращает объект типа ConditionFactory с настройками по умолчанию для ожиданий.
     * @return объект ConditionFactory
     */
    public ConditionFactory getDefaultSettings() {
        return Awaitility.given()
                .timeout(envSettings.getApiTimeoutInSeconds(), TimeUnit.SECONDS)
                .pollInterval(envSettings.getApiIntervalInSeconds(), TimeUnit.SECONDS)
                .pollDelay(0, TimeUnit.SECONDS)
                .pollInSameThread();
    }

    /**
     * Выполняет ожидание выполнения условия, определенного предикатом для HTTP-запроса.
     * @param request HTTP-запрос
     * @param conditionAlias условия ожидания
     * @param condition предикат, определяющий условие ожидания для HTTP-ответа
     */
    public void waitUntil(AtHttpRequest request, String conditionAlias, Predicate<AtHttpResponse<Object>> condition) {
        getDefaultSettings()
                .alias(conditionAlias)
                .until(() -> {
                    AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
                    return condition.test(response);
                });
    }

    /**
     * Выполняет ожидание заданного условия, определенного предикатом для HTTP-запроса с использованием условия опроса.
     * @param waitSettings условия опроса
     * @param request HTTP-запрос
     * @param condition предикат, определяющий условие ожидания для HTTP-ответа
     */
    public void waitUntil(ConditionFactory waitSettings, AtHttpRequest request, Predicate<AtHttpResponse<Object>> condition) {
        waitSettings
                .until(() -> {
                    AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
                    return condition.test(response);
                });
    }

    /**
     * Выполняет ожидание заданного условия, определенного предикатом путем выполнения операции с использованием условия опроса.
     * @param waitSettings условия опроса
     * @param operation поставщик (Supplier) результата операции, которую необходимо выполнить
     * @param predicate предикат, определяющий условие ожидания для возвращаемого результата операции
     */
    public <T> void waitUntil(ConditionFactory waitSettings, Supplier<T> operation, Predicate<T> predicate) {
        waitSettings.until(() -> {
            T result = operation.get();
            return predicate.test(result);
        });
    }

    /**
     * @param waitSettings условия опроса
     * @param condition    ожидаемое выражение
     *                     Метод прерывает иные ожидания
     *                     Пример:
     *                     дожидаемся что WebElement пропал, спустя заданное время, а не выжидая глобальный таймаут
     */
    public boolean waitUntil(ConditionFactory waitSettings, Callable<Boolean> condition) {
        try {
            return waitSettings
                    .pollExecutorService(Executors.newSingleThreadExecutor())
                    .until(condition, Boolean::booleanValue);
        } catch (ConditionTimeoutException e) {
            log.warn(e.getMessage());
            return false;
        }
    }
}
