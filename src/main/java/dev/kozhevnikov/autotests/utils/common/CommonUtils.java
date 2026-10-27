package dev.kozhevnikov.autotests.utils.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;

import java.io.File;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Содержит методы для работы с файлами, JSON, обогащения данных,ожидания и т.д.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class CommonUtils {
    private final FileUtils fileUtils;
    private final JsonUtils jsonUtils;
    private final Enricher enricher;
    private final ServiceUtils serviceUtils;
    private final WaitUtils waitUtils;

    /**
     * Преобразует строку в формат Base64.
     * @param str строка для кодирования в формат Base64
     * @return строка в формате Base64, представляющая входную строку
     */
    public String toBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    /**
     * Преобразует содержимое файла в формат Base64.
     * @param fileName имя файла
     * @return содержимое файла в формате Base64
     */
    public String fileToBase64(String fileName) {
        File file = fileUtils.getResourceFile(fileName);
        return fileUtils.toBase64(file);
    }

    /**
     * Декодирует строку из формата Base64.
     * @param base64str строка в формате Base64 для декодирования
     * @return декодированная строка
     */
    public String fromBase64(String base64str) {
        byte[] bytes = Base64.getDecoder().decode(base64str);
        return new String(bytes);
    }

    /**
     * Проверяет объект на null.
     * @param obj объект для проверки
     * @return {@code true}, если объект равен {@code null} или строке "null", если нет - {@code false}
     */
    public boolean isNull(Object obj) {
        return obj == null || obj.equals("null");
    }

    /**
     * Обогащает списки строк.
     * @param params список списков строк для обогащения
     * @return обогащенный список списков строк
     */
    public List<List<String>> enrichLists(List<List<String>> params) {
        return params.stream()
                .map(enricher::enrich)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает JSON-строку, сформированную на основе данных из списка Map.
     * @param templatePath путь к шаблону
     * @param params список Map, содержащих данные для формирования JSON
     * @return JSON-строка, сформированная на основе данных из списка Map
     */
    public String getJsonByPathForListMap(String templatePath, List<Map<String, String>> params) {
        return params.stream().map(object -> enricher.enrich(fileUtils.readResourceTemplate(templatePath), enricher.enrich(object)))
                .collect(Collectors.toList()).toString();
    }

    /**
     * Возвращает объект запроса на основе заданного сервиса и URI.
     * @param service сервис для использования в запросе
     * @param uri URI для запроса
     * @return сформированный запрос
     */
    public AtHttpRequest getRequest(ServiceConfig service, String uri) {
        String enrichedUri = enricher.enrich(uri);

        return serviceUtils.generateHttpRequest(service)
                .resolveUri(enrichedUri)
                .setMethod(HttpMethod.GET);
    }

    /**
     * Ожидает выполнения условий HTTP-запроса на основе пути и параметров.
     * @param request HTTP-запрос
     * @param path путь для ожидания
     * @param params параметры для проверки выполнения условий
     * @param equals флаг, определяющий строгое равенство или вхождение
     */
    public void waitForValue(AtHttpRequest request, String path, Map<String, String> params, boolean equals) {
        String enriched = enricher.enrich(path);
        Map<String, String> enrichedParams = enricher.enrich(params);
        waitUtils.waitUntil(request, "Ожидание выполнения условий " + enrichedParams,
                response -> enrichedParams.entrySet().stream()
                        .allMatch(entry -> {
                            String value = jsonUtils.getAsStringByPath(response.getBody(), enriched);
                            boolean actual;
                            if (equals) {
                                actual = value.equals(entry.getKey());
                            } else {
                                actual = value.contains(entry.getKey());
                            }
                            return actual == Boolean.parseBoolean(entry.getValue());
                        }));
    }

    /**
     * Преобразует Map из строк в обогащенную Map с объектами.
     * @param map исходная Map строк для обогащения
     * @return обогащенная Map с объектами
     */
    public Map<String, Object> toEnrichedJsonMap(Map<String, String> map) {
        Map<String, Object> result = new HashMap<>();
        enricher.enrich(map).entrySet().forEach(entry -> result.put(entry.getKey(), getFunction().apply(entry)));
        return result;
    }

    /**
     * Возвращает обогащенный шаблон c учетом параметров в Map.
     * @param templateName шаблона
     * @param map карта параметров для обогащения
     * @return обогащенный шаблон
     */
    public String getEnrichedTemplate(String templateName, Map<String, String> map) {
        String template = fileUtils.readResourceTemplate(templateName);
        Map<String, String> enrichedMap = enricher.enrich(map);

        return enricher.enrich(template, enrichedMap);
    }

    private List<Object> createListFromTemplate(String enrichedTemplate, List<Map<String, String>> params) {
        return params.stream()
                .map(enricher::enrich)
                .map(object -> enricher.enrich(enrichedTemplate, object))
                .map(jsonUtils::unify)
                .map(object -> jsonUtils.fromJson(object, Object.class))
                .collect(Collectors.toList());
    }

    /**
     * Создает список объектов на основе шаблона из папки Test и списка Map с параметрами.
     * @param template шаблон из папки Test
     * @param params список Map с параметрами
     * @return список объектов
     */
    public List<Object> createListFromTestTemplate(String template, List<Map<String, String>> params) {
        return createListFromTemplate(fileUtils.readResourceTemplate(template), params);
    }

    /**
     * Создает список объектов на основе шаблона из папки Main и списка Map параметров.
     * @param template шаблон из папки Main
     * @param params список карт параметров
     * @return список объектов
     */
    public List<Object> createListFromMainTemplate(String template, List<Map<String, String>> params) {
        return createListFromTemplate(fileUtils.readMainResourceTemplate(template), params);
    }

    /**
     * Удаляет значения null из Map.
     * @param map исходная
     * @param <K> ключ
     * @param <V> значение
     * @return Map без значений null
     */
    public <K, V> Map<K, V> removeNullValues(Map<K, V> map) {
        return Objects.requireNonNullElse(map, Collections.<K, V>emptyMap()).entrySet()
                .stream()
                .filter(entry -> !Objects.isNull(entry.getValue()))
                .filter(entry -> !entry.getValue().equals("null"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Преобразует два списка в Map.
     * @param keys список ключей
     * @param values список значений
     * @return Map, содержащая пары ключ-значение
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если размер списка ключей не совпадает с размером списка значений
     */
    public Map<String, String> joinListsToMap(List<String> keys, List<String> values) {
        if (keys.size() != values.size()) {
            throw new AutotestException("List size keys does not match list values");
        }

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.get(i));
        }

        return result;
    }

    private Function<Map.Entry<String, String>, Object> getFunction() {
        return entry -> {
            try {
                return jsonUtils.fromJson(entry.getValue(), Object.class);
            } catch (Exception e) {
                return entry.getValue();
            }
        };
    }
}