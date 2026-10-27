package dev.kozhevnikov.autotests.utils.api;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Вспомогательный класс для работы с сервисами.
 *
 * @see ServiceConfig
 * @see AtHttpRequest
 * @see AtHttpResponse
 */
@Component
@Lazy
@Slf4j
@EqualsAndHashCode
@RequiredArgsConstructor
public class ServiceUtils {

    private final WebClient httpWebClient;
    private final Enricher enricher;
    private final EnvSettings envSettings;
    private final FileUtils fileUtils;

    /**
     * Отправляет HTTP-запрос и получает ответ с телом заданного типа.
     * Если код ответа меньше 400, в лог выводится сокращенная запись (без заголовков запроса).
     *
     * @param httpRequest       отправляемый HTTP-запрос
     * @param responseBodyClass требуемый тип тела ответа
     * @return ответ с телом заданного типа
     * @throws dev.kozhevnikov.autotests.exception.AutotestException при любой ошибке в ходе выполнения метода
     */
    public <T> AtHttpResponse<T> sendHttpRequest(AtHttpRequest httpRequest, Class<T> responseBodyClass) {

        try {
            // exchangeToMono не бросает исключение на 4xx/5xx: статус проверяется в шагах
            AtHttpResponse<T> httpResponse = httpWebClient
                    .method(httpRequest.getMethod())
                    .uri(generateUri(httpRequest))
                    .headers(headers -> headers.putAll(toMultiMap(httpRequest.getHeaders())))
                    .body(toBodyInserter(httpRequest))
                    .exchangeToMono(response -> {
                        Map<String, String> resultHeaders = response.headers()
                                .asHttpHeaders()
                                .toSingleValueMap();
                        int status = response.statusCode().value();
                        return response.bodyToMono(responseBodyClass)
                                .map(body -> new AtHttpResponse<>(status, resultHeaders, body))
                                .defaultIfEmpty(new AtHttpResponse<>(status, resultHeaders, null));
                    })
                    .timeout(Duration.ofMillis(httpRequest.getTimeout()))
                    .block();
            Objects.requireNonNull(httpResponse, String.format("Ответ на %s  - null", httpRequest));

            AtHttpRequest requestForLog = httpResponse.getStatusCode() < 400 ? filterHttpRequest(httpRequest) : httpRequest;
            log.info("Отправлен запрос:\n{}\n\nПолучен ответ:\n{}", requestForLog, httpResponse);

            return httpResponse;
        } catch (Exception e) {
            throw new AutotestException(String.format("Не удалось получить ответ на запрос:%n%s%n%nОшибка:%s", httpRequest, e.getMessage()));
        }
    }

    /**
     * Отправляет HTTP-запрос и получает ответс телом типа {@code Object}.
     * Вызывает метод {@link ServiceUtils#sendHttpRequest}.
     *
     * @param httpRequest отправляемый HTTP-запрос
     * @return ответ с телом типа {@code Object}
     */
    public AtHttpResponse<Object> sendHttpRequest(AtHttpRequest httpRequest) {
        return sendHttpRequest(httpRequest, Object.class);
    }

    /**
     * Генерирует HTTP-запрос для заданного сервиса с целью применения в генерации тестовых данных.
     * Вызывает метод {@link ServiceUtils#generateHttpRequest}.
     * В заголовки HTTP-запроса добавляется токен авторизации.
     *
     * @param service сервис, для которого генерируется HTTP-запрос
     * @return сформированный HTTP-запрос
     */
    public AtHttpRequest getRequestForGenerate(ServiceConfig service) {
        AtHttpRequest request = generateHttpRequest(service);

        request.getHeaders().put("authorization", "Bearer " + envSettings.getStartToken());
        return request;
    }

    /**
     * Генерирует базовый шаблон HTTP-запроса (URL,порт,заголовки,время ожидания ответа) для заданного сервиса
     *
     * @param service сервис, для которого генерируется HTTP-запрос
     * @return сформированный HTTP-запрос
     */
    public AtHttpRequest generateHttpRequest(ServiceConfig service) {
        return new AtHttpRequest()
                .setUri(URI.create(service.getUrl()))
                .setPort(service.getPort())
                .setHeaders(enricher.enrich(service.getHeaders()))
                .setTimeout(service.getTimeout());
    }

    /**
     * Создает единичный параметр для HTTP-запроса в виде пары ключ-значение.
     * Вызывает метод {@link ServiceUtils#createQueryParams}.
     *
     * @param key   имя параметра
     * @param value значение параметра
     * @return сформированный параметр HTTP-запроса
     * @see AtHttpRequest
     */
    public Map<String, String> createQueryParam(String key, Object value) {
        return createQueryParams(new String[]{key}, new Object[]{value});
    }

    /**
     * Создает набор параметров для HTTP-запроса в виде пар ключ-значение с заменой {@code null} на пустую строку
     *
     * @param keys   набор имен параметров
     * @param values набор значений параметров
     * @return сформированный набор параметров HTTP-запроса
     * @throws dev.kozhevnikov.autotests.exception.AutotestException при несовпадении числа параметров с числом значений
     * @see AtHttpRequest
     */
    public Map<String, String> createQueryParams(String[] keys, Object[] values) {
        if (keys.length == values.length) {
            Map<String, Object> queryParams = Arrays.stream(keys)
                    .collect(Collectors.toMap(
                            key -> key,
                            key -> values[Arrays.asList(keys).indexOf(key)] == null ? "" : values[Arrays.asList(keys).indexOf(key)]
                    ));
            return createQueryParams(queryParams);

        } else {
            throw new AutotestException(String.format("Число ключей и значений для параметров запроса неодинаково: ключей %d, значений %d", keys.length, values.length));
        }
    }

    /**
     * Создает набор параметров для HTTP-запроса на основе уже имеющегося списка параметров с заменой {@code null}
     * на пустую строку
     *
     * @param queryParams список параметров
     * @return созданный набор параметров
     * @see AtHttpRequest
     */
    public Map<String, String> createQueryParams(Map<String, Object> queryParams) {
        return queryParams.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() == null ? "" : entry.getValue().toString()));
    }

    private AtHttpRequest filterHttpRequest(AtHttpRequest basicRequest) {
        AtHttpRequest newRequest = new AtHttpRequest();

        newRequest.setUri(basicRequest.getUri());
        newRequest.setPort(basicRequest.getPort());
        newRequest.setTimeout(basicRequest.getTimeout());
        newRequest.setMethod(basicRequest.getMethod());
        newRequest.setQueryParams(basicRequest.getQueryParams());
        newRequest.setBody(basicRequest.getBody());

        return newRequest;
    }

    private URI generateUri(AtHttpRequest request) {
        return UriComponentsBuilder.fromUri(request.getUri())
                .port(request.getPort())
                .queryParams(CollectionUtils.toMultiValueMap(toMultiMap(request.getQueryParams())))
                .encode()
                .build()
                .toUri();
    }

    private BodyInserter<?, ? super ClientHttpRequest> toBodyInserter(AtHttpRequest request) {
        if (request.getBody() != null && request.getFiles() != null) {
            throw new AutotestException("Необработанное поведение: в запросе присутствуют body и files");
        } else if (request.getBody() != null) {
            return BodyInserters.fromValue(request.getBody());
        } else if (request.getFiles() != null) {
            MultipartBodyBuilder multipart = new MultipartBodyBuilder();
            request.getFiles().forEach((key, value) ->
            {
                String fileType = value.getName().split("\\.")[1];
                MultipartBodyBuilder.PartBuilder part = multipart.part(key, fileUtils.toByteArray(value))
                        .header("Content-Disposition", "form-data; name=" + key + "; filename=" + key);
                switch (fileType) {
                    case "json":
                        part.header("Content-Type", "application/json;charset=UTF-8");
                    case "png":
                        part.header("Content-Type", "image/png");
                    case "jpg":
                        part.header("Content-Type", "image/jpeg");
                }
            });
            return BodyInserters.fromMultipartData(multipart.build());
        } else {
            return BodyInserters.empty();
        }
    }

    private Map<String, List<String>> toMultiMap(Map<String, String> map) {
        return Objects.requireNonNullElse(map, Collections.<String, String>emptyMap()).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.of(entry.getValue())
                ));
    }
}
