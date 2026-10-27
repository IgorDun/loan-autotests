package dev.kozhevnikov.autotests.utils.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.exception.AutotestException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Обрабатывает данные в формате JSON.
 */
@Component
@Lazy
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class JsonUtils {
    /**
     *Устанавливает конфигурацию JSON для работы с датами и перечислениями.
     *Устанавливает формат даты и время на "yyyy-MM-dd'T'HH:mm:ss" для JSON-парсера и JSON-генератора.
     *Также устанавливает опцию для JSON-генератора, чтобы он писал перечисления используя их имена, и чтобы писал значение null.
     */
    static {
        JSON.configReaderDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        JSON.config(JSONWriter.Feature.WriteEnumsUsingName, JSONWriter.Feature.WriteNulls);
    }

    /**
     * Парсит JSON-строку в объект заданного типа.
     *
     * @param json JSON-строка, которую необходимо распарсить в объект.
     * @param type Тип объекта, в который необходимо преобразовать JSON.
     * @return Объект заданного типа, полученный из JSON-строки.
     */
    public <T> T fromJson(String json, Class<T> type) {
        return JSON.parseObject(json, type);
    }

    /**
     * Преобразует объект в JSON или парсит JSON-строку в объект.
     * Если объект является строкой и является валидным JSON, то он будет распарсен с параметром IgnoreSetNullValue.
     * Если объект не является строкой, то он будет преобразован в JSON-строку, которая затем будет распарсена с параметром IgnoreSetNullValue.
     *
     * @param obj - Объект, который необходимо преобразовать в JSON или JSON-строка для распарсивания
     * @return Объект, полученный из JSON-строки или JSON-строка из объекта
     */
    public Object toJson(Object obj) {
        if (obj instanceof String && JSON.isValid((String) obj)) {
            return JSON.parse((String) obj, JSONReader.Feature.IgnoreSetNullValue);
        }
        return JSON.parse(toJsonString(obj), JSONReader.Feature.IgnoreSetNullValue);
    }

    /**
     * Преобразует объект в строку JSON.
     *
     * @param obj объект для преобразования в JSON-строку
     * @return JSON-строка, представляющая объект
     */
    public <T> String toJsonString(T obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * Удаляет поля из JSON-объекта по указанному пути.
     *
     * @param json JSON-объект, из которого удаляется поле
     * @param path путь к полю в формате JSONPath
     */
    public void removeField(Object json, String path) {
        JSONPath.remove(json, path);
    }

    /**
     * Устанавливает значения поля в JSON-объекте по указанному пути.
     *
     * @param json  JSON-объект, в котором устанавливается значение
     * @param path  путь к полю в формате JSONPath
     * @param value значение, которое устанавливается
     * @return обновленный JSON-объект после установки значения
     */
    public Object setValue(Object json, String path, Object value) {
        return JSONPath.set(json, path, value);
    }

    /**
     * Проверяет значения поля в JSON-объекте по указанному пути.
     *
     * @param obj           JSON-объект, в котором происходит проверка значения
     * @param path          путь к полю в формате JSONPath
     * @param requiredValue требуемое значение для сравнения
     * @return true, если значение по указанному пути совпадает с требуемым значением, если нет - false
     */
    public boolean checkValue(Object obj, String path, Object requiredValue) {
        return Optional.of(getAsStringByPath(obj, path))
                .map(value -> value.equals(requiredValue))
                .orElse(false);
    }

    /**
     * Получает значения поля в JSON-объекте по указанному пути и преобразует его в строку.
     *
     * @param json JSON-объект, из которого извлекается значение по пути
     * @param path путь к полю в формате JSONPath
     * @return строковое представление значения поля по указанному пути
     */
    public String getAsStringByPath(Object json, String path) {
        return unify(toJsonString(evaluateJsonPath(json, path)));
    }

    /**
     * Получает значения поля в JSON-объекте по указанному пути и преобразует его в указанный тип.
     *
     * @param json JSON-объект, из которого извлекается значение по пути
     * @param path путь к полю в формате JSONPath
     * @param <T>  тип значения (generic)
     * @return значение поля по указанному пути, преобразованное в указанный тип
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если значение по указанному пути не найдено
     */
    @SuppressWarnings("unchecked")
    public <T> T getAsTypeByPath(Object json, String path) {
        return Optional.ofNullable(evaluateJsonPath(json, path))
                .map(jsonResult -> (T) JSON.parseObject(JSON.toJSONBytes(jsonResult), jsonResult.getClass()))
                .orElseThrow(() -> new AutotestException(String.format("Не найдено значение по path - %s%nв объекте - %s", path, json)));
    }

    /**
     * Осуществляет поиск и получение значения поля в JSON-объекте по указанному пути и преобразует его в указанный тип.
     *
     * @param json JSON-объект, из которого извлекается значение по пути
     * @param path путь к полю в формате JSONPath
     * @param <T>  тип значения (generic)
     * @return Optional, содержащий значение поля по указанному пути, преобразованное в указанный тип, если оно присутствует, иначе пустой Optional.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> findAsTypeByPath(Object json, String path) {
        return Optional.ofNullable(evaluateJsonPath(json, path))
                .map(jsonResult -> (T) JSON.parseObject(JSON.toJSONBytes(jsonResult), jsonResult.getClass()));
    }

    /**
     * Красиво форматирует объект в строку JSON.
     *
     * @param obj объект для форматирования в JSON-строку
     * @param <T> тип объекта (generic)
     * @return красиво отформатированная JSON-строка, представляющая объект.
     */
    public <T> String prettify(T obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
    }

    /**
     * Преобразует входную строку в формате JSON, удаляя лишние кавычки и заменяя пустые строки на {@code null}.
     *
     * @param json строка в формате JSON
     * @return преобразованная строка *
     */
    public String unify(String json) {
        return json
                .replaceAll("^\"(.+)\"$", "$1")
                .replace("\"\"", "null")
                .replace("\"null\"", "null")
                .replaceAll(":(\\s*),", ": null,")
                .replaceAll(":(\\s*)}", ": null}")
                .replace("\\\\\"", "\"")
                .replace("\\\"", "\"")
                .replaceAll("\"([{\\[])", "$1")
                .replaceAll("([}\\]])\"", "$1")
                .replace("\\n", "")
                .replaceAll("\\\\\"([А-Яа-яЁё\\s]+)\\\\\"(\")", "'$1'$2");
    }

    private Object evaluateJsonPath(Object json, String path) {
        Object jsonObj = json;
        if (jsonObj instanceof String) {
            String jsonStr = (String) jsonObj;
            if (jsonStr.matches("^\\{(.+)}$")) {
                jsonObj = fromJson(jsonStr, Map.class);
            } else if (jsonStr.matches("^\\[(.+)]$")) {
                jsonObj = fromJson(jsonStr, List.class);
            }
        }

        return JSONPath.of(path).eval(jsonObj);
    }
}
