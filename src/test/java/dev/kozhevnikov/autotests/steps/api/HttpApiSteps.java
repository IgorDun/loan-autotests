package dev.kozhevnikov.autotests.steps.api;

import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.config.ServiceConfiguration;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Универсальные шаги HTTP для любого сервиса из services.properties.
 * Ответ сохраняется в контекст: тело под именем ответа, код под именем с суффиксом ".status".
 * <pre>
 * When dummyjson > Отправляю GET запрос на '/products/1' и сохраняю ответ как 'product'
 * Then Проверяю, что код ответа 'product' равен 200
 * And Проверяю в json 'product' поля:
 *   | $.id | 1 |
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class HttpApiSteps {
    private static final String STATUS_SUFFIX = ".status";

    private final ServiceConfiguration serviceConfiguration;
    private final ServiceUtils serviceUtils;
    private final JsonUtils jsonUtils;
    private final Enricher enricher;

    @When("^(.+) > Отправляю (GET|DELETE) запрос на '(.+)' и сохраняю ответ как '(.+)'$")
    public void sendWithoutBody(String service, String method, String path, String responseName) {
        send(service, method, path, null, null, responseName);
    }

    @When("^(.+) > Отправляю (GET|DELETE) запрос на '(.+)' с параметрами и сохраняю ответ как '(.+)':$")
    public void sendWithQuery(String service, String method, String path, String responseName, Map<String, String> query) {
        send(service, method, path, enricher.enrich(query), null, responseName);
    }

    @When("^(.+) > Отправляю (POST|PUT|PATCH) запрос на '(.+)' с телом и сохраняю ответ как '(.+)':$")
    public void sendWithBody(String service, String method, String path, String responseName, String body) {
        Object json = jsonUtils.toJson(jsonUtils.unify(enricher.enrich(body)));
        send(service, method, path, null, json, responseName);
    }

    @When("^(.+) > Отправляю (POST|PUT|PATCH) запрос на '(.+)' с полями и сохраняю ответ как '(.+)':$")
    public void sendWithFields(String service, String method, String path, String responseName, Map<String, String> fields) {
        send(service, method, path, null, enricher.enrich(fields), responseName);
    }

    @Then("^Проверяю, что код ответа '(.+)' равен (\\d+)$")
    public void checkStatus(String responseName, int expected) {
        Object actual = AutoTestContext.getNotNull(responseName + STATUS_SUFFIX);
        Assertions.assertThat(actual)
                .withFailMessage("Код ответа '%s': ожидался %s, получен %s", responseName, expected, actual)
                .isEqualTo(expected);
    }

    private void send(String service, String method, String path, Map<String, String> query, Object body, String responseName) {
        AtHttpRequest request = serviceUtils.generateHttpRequest(serviceConfiguration.getService(service))
                .resolveUri(enricher.enrich(path))
                .setMethod(HttpMethod.valueOf(method))
                .setBody(body);
        if (query != null) {
            request.setQueryParams(query);
        }
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        AutoTestContext.put(responseName, response.getBody());
        AutoTestContext.put(responseName + STATUS_SUFFIX, response.getStatusCode());
        log.info("{} > {} {} -> {}, ответ сохранён как '{}'", service, method, path, response.getStatusCode(), responseName);
    }
}
