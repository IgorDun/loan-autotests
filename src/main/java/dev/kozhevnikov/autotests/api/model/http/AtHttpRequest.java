package dev.kozhevnikov.autotests.api.model.http;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * Основной класс для создания HTTP-запросов.
 * Объекты этого класса являются изменяемыми и могут переиспользоваться для модификации запросов.
 * Компоненты базового шаблона HTTP-запроса:
 * <ul>
 * <li>uri - uri запроса;</li>
 * <li>port  - используемый порт;</li>
 * <li>timeout - время ожидания ответа;</li>
 * <li>method - HTTP-метод запроса;</li>
 * <li>headers - заголовки запроса;</li>
 * <li>queryParam - параметры запроса;</li>
 * <li>body - тело запроса;</li>
 * <li>files - файлы запроса.</li>
 * </ul>
 * Объекты класса {@code AtHttpRequest} можно конфигурировать при помощи цепочки вызовов, например:
 * {@code new AtHttpRequest()
 * .setUri()
 * .setPort()
 * .setTimeout()
 * .setMethod()
 * .setHeaders()
 * .setQueryParam()
 * .setBody()
 * .setFiles()}
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class AtHttpRequest {
    private URI uri;
    private Integer port;
    private Long timeout;
    private HttpMethod method;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Object body;
    private Map<String, File> files;

    /**
     * К имеющемуся URI добавляется переданный параметр.
     *
     * @param path - путь к конкретному объекту внутри микросервиса, который добавляется к текущему URI в HTTP-запросе
     * @return текущий объект {@code AtHttpRequest} с обновленным URI
     */
    public AtHttpRequest resolveUri(String path) {
        String uriString = uri.toString();
        StringBuilder stringBuilder = new StringBuilder(uriString);
        if (!uriString.endsWith("/") && !path.startsWith("/")) {
            stringBuilder.append("/");
        } else if (uriString.endsWith("/") && path.startsWith("/")) {
            stringBuilder.deleteCharAt(uriString.lastIndexOf("/"));
        }
        stringBuilder.append(path);
        this.uri = URI.create(stringBuilder.toString());
        return this;
    }

    /**
     * Возвращает представление HTTP-запроса в виде строки с указанием содержимого в удобочитаемом виде по шаблону:
     * <pre>
     * URI: http://exaple.com/path
     * Port: 8080
     * Timeout: 1000
     * Method: POST
     * Headers: {Accept:application/json}
     * Query params: {param:value}
     * Body: {param1:value1,param2:value2}
     * </pre>
     *
     * @return строковое представление текущего объекта {@code AtHttpRequest}
     */
    @Override
    public String toString() {
        return String.format("URI: %s%nPort: %s%nTimeout: %s%nMethod: %s%nHeaders: %s%nQuery params: %s%nBody: %s",
                uri, port, timeout, method, headers, queryParams, body);
    }
}
