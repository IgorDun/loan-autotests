package dev.kozhevnikov.autotests.elastic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Ответ _search: из него нужны только документы hits.hits[]._source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResponse(Hits hits) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hits(List<Hit> hits) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hit(@JsonProperty("_index") String index,
                      @JsonProperty("_source") Map<String, Object> source) {
    }

    public List<Map<String, Object>> sources() {
        if (hits == null || hits.hits() == null) {
            return List.of();
        }
        return hits.hits().stream().map(Hit::source).toList();
    }
}
