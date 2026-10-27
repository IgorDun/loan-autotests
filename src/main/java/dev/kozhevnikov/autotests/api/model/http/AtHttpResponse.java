package dev.kozhevnikov.autotests.api.model.http;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

/**
 * Основной класс для формирования ответов.
 * Компоненты базового шаблона ответа:
 * <ul>
 * <li>statusCode - код ответа;</li>
 * <li>headers - заголовки ответа;</li>
 * <li>body - тело ответа;</li>
 * </ul>
 * Объекты класса {@code AtHttpResponse} можно конфигурировать при помощи цепочки вызовов, например:
 * {@code new AtHttpResponse<>(response.statusCode().value(), resultHeaders, responseBody)}
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class AtHttpResponse<T> {
    private int statusCode;
    private Map<String, String> headers;
    private T body;

    /**
     * Возвращает представление ответа в виде строки с указанием содержимого в удобочитаемом виде по шаблону:
     * <pre>
     * Status code: 200
     * Headers: {Accept:application/json}
     * Body: {param1:value1,param2:value2}
     * </pre>
     *
     * @return строковое представление текущего объекта {@code AtHttpResponse}
     */
    @Override
    public String toString() {
        return String.format("Status code: %s%nHeaders: %s%nBody: %s", statusCode, headers, body);
    }
}
