package dev.kozhevnikov.autotests.elastic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Тело запроса _search: размер выборки, поля source, bool-запрос и сортировка по времени.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record SearchRequest(int size,
                            @JsonProperty("_source") List<String> source,
                            Map<String, Object> query,
                            List<Map<String, Object>> sort) {
}
