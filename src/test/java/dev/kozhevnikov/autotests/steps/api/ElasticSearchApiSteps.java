package dev.kozhevnikov.autotests.steps.api;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionFactory;
import dev.kozhevnikov.autotests.elastic.ElasticSearchService;
import dev.kozhevnikov.autotests.elastic.SearchRequest;
import dev.kozhevnikov.autotests.elastic.SearchRequestBuilder;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.utils.CucumberUtils;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.WaitUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ElasticSearchApiSteps {
    private final ElasticSearchService service;
    private final JsonUtils jsonUtils;
    private final CucumberUtils cucumberUtils;
    private final Enricher enricher;
    private final WaitUtils waitUtils;
    private final EnvSettings envSettings;

    /**
     * @param size     - количество логов, которые метод получит для обработки
     * @param type     - шаблон для поиска с параметром filebeat или packetbeat
     *                 для filebeat обрабатывается текст из поля 'message'
     *                 для packetbeat обрабатывается текст из поля 'http.request.body.content'
     * @param logsName - ключ для сохранения в контекст
     * @param table    - таблица в виде | condition | field | value |, где
     *                 condition - одно из условий: [mustExists, filterExists, shouldExists, mustNotExists,
     *                 mustMatchPhrase, filterMatchPhrase, shouldMatchPhrase, mustNotMatchPhrase, filterMultiMatch]
     *                 field - название поля
     *                 value - значение поля
     */
    @When("^elastic-search > Получаю (\\d+) логов из (filebeat|packetbeat) по параметрам и сохраняю как '(.+)':$")
    public void searchLogs(Integer size, String type, String logsName, DataTable table) {
        List<List<String>> params = table.asLists()
                .stream()
                .map(enricher::enrich)
                .collect(Collectors.toList());
        SearchRequestBuilder requestBuilder = service.searchRequest()
                .size(size)
                .source((type.equals("filebeat")) ? "message" : "http.request.body.content")
                .source("@timestamp");

        fillMustExistsConditions(requestBuilder, params);
        fillFilterExistsConditions(requestBuilder, params);
        fillShouldExistsConditions(requestBuilder, params);
        fillMustNotExistsConditions(requestBuilder, params);
        fillMustMatchPhraseConditions(requestBuilder, params);
        fillFilterMatchPhraseConditions(requestBuilder, params);
        fillShouldMatchPhraseConditions(requestBuilder, params);
        fillMustNotMatchPhraseConditions(requestBuilder, params);
        fillFilterMultiMatchConditions(requestBuilder, params);
        SearchRequest request = requestBuilder.build();

        log.info("Request: {}", jsonUtils.toJsonString(request));

        ConditionFactory waitSettings = waitUtils.getDefaultSettings()
                .timeout(Duration.ofSeconds(envSettings.getLogsLongTimeoutInSeconds()))
                .await("Ожидание получения логов по запросу: " + jsonUtils.toJsonString(request));
        waitUtils.waitUntil(waitSettings, () -> service.search(request),
                response -> !response.sources().isEmpty());
        log.info("Получены логи по запросу: {}", jsonUtils.toJsonString(request));

        List<Map<String, Object>> response = service.search(request).sources();
        Object json = jsonUtils.toJson(response);
        log.info("Найденный логи: {}", json);

        AutoTestContext.put(logsName, json);
    }

    private void fillMustExistsConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("mustExists"))
                .forEach(param -> builder.mustExists(param.get(1)));
    }

    private void fillFilterExistsConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("filterExists"))
                .forEach(param -> builder.filterExists(param.get(1)));
    }

    private void fillShouldExistsConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("shouldExists"))
                .forEach(param -> builder.shouldExists(param.get(1)));
    }

    private void fillMustNotExistsConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("mustNotExists"))
                .forEach(param -> builder.mustNotExists(param.get(1)));
    }

    private void fillMustMatchPhraseConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("mustMatchPhrase"))
                .forEach(param -> builder.mustMatchPhrase(param.get(1), param.get(2)));
    }

    private void fillFilterMatchPhraseConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("filterMatchPhrase"))
                .forEach(param -> builder.filterMatchPhrase(param.get(1), param.get(2)));
    }

    private void fillShouldMatchPhraseConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("shouldMatchPhrase"))
                .forEach(param -> builder.shouldMatchPhrase(param.get(1), param.get(2)));
    }

    private void fillMustNotMatchPhraseConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("mustNotMatchPhrase"))
                .forEach(param -> builder.mustNotMatchPhrase(param.get(1), param.get(2)));
    }

    private void fillFilterMultiMatchConditions(SearchRequestBuilder builder, List<List<String>> params) {
        cucumberUtils.filterRows(params, row -> row.get(0).equals("filterMultiMatch"))
                .forEach(param -> builder.filterMultiMatch("phrase", param.get(2), true));
    }
}
