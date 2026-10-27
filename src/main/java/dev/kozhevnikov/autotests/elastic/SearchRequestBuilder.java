package dev.kozhevnikov.autotests.elastic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Построитель bool-запроса Elasticsearch: must / filter / should / must_not
 * с условиями exists, match_phrase и multi_match.
 */
public class SearchRequestBuilder {
    private int size = 10;
    private final List<String> source = new ArrayList<>();
    private final List<Map<String, Object>> must = new ArrayList<>();
    private final List<Map<String, Object>> filter = new ArrayList<>();
    private final List<Map<String, Object>> should = new ArrayList<>();
    private final List<Map<String, Object>> mustNot = new ArrayList<>();

    public SearchRequestBuilder size(int size) {
        this.size = size;
        return this;
    }

    public SearchRequestBuilder source(String field) {
        source.add(field);
        return this;
    }

    public SearchRequestBuilder mustExists(String field) {
        must.add(exists(field));
        return this;
    }

    public SearchRequestBuilder filterExists(String field) {
        filter.add(exists(field));
        return this;
    }

    public SearchRequestBuilder shouldExists(String field) {
        should.add(exists(field));
        return this;
    }

    public SearchRequestBuilder mustNotExists(String field) {
        mustNot.add(exists(field));
        return this;
    }

    public SearchRequestBuilder mustMatchPhrase(String field, String value) {
        must.add(matchPhrase(field, value));
        return this;
    }

    public SearchRequestBuilder filterMatchPhrase(String field, String value) {
        filter.add(matchPhrase(field, value));
        return this;
    }

    public SearchRequestBuilder shouldMatchPhrase(String field, String value) {
        should.add(matchPhrase(field, value));
        return this;
    }

    public SearchRequestBuilder mustNotMatchPhrase(String field, String value) {
        mustNot.add(matchPhrase(field, value));
        return this;
    }

    public SearchRequestBuilder filterMultiMatch(String type, String query, boolean lenient) {
        filter.add(Map.of("multi_match", Map.of("type", type, "query", query, "lenient", lenient)));
        return this;
    }

    public SearchRequest build() {
        Map<String, Object> bool = new LinkedHashMap<>();
        putIfNotEmpty(bool, "must", must);
        putIfNotEmpty(bool, "filter", filter);
        putIfNotEmpty(bool, "should", should);
        putIfNotEmpty(bool, "must_not", mustNot);
        if (!should.isEmpty()) {
            bool.put("minimum_should_match", 1);
        }
        return new SearchRequest(size, List.copyOf(source), Map.of("bool", bool),
                List.of(Map.of("@timestamp", Map.of("order", "desc"))));
    }

    private static Map<String, Object> exists(String field) {
        return Map.of("exists", Map.of("field", field));
    }

    private static Map<String, Object> matchPhrase(String field, String value) {
        return Map.of("match_phrase", Map.of(field, value));
    }

    private static void putIfNotEmpty(Map<String, Object> target, String key, List<Map<String, Object>> clauses) {
        if (!clauses.isEmpty()) {
            target.put(key, List.copyOf(clauses));
        }
    }
}
