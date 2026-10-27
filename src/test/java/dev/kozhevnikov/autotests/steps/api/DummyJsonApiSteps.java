package dev.kozhevnikov.autotests.steps.api;

import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.CommonUtils;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Шаги сервиса dummyjson.com. Один шаг — одно бизнес-действие: URI, метод, шаблон тела
 * и ожидаемый код ответа задаются внутри шага, сценарий передаёт только данные.
 */
@Slf4j
@RequiredArgsConstructor
public class DummyJsonApiSteps {
    private final JsonUtils jsonUtils;
    private final Enricher enricher;
    private final ServiceUtils serviceUtils;
    private final CommonUtils commonUtils;
    private final ServiceConfig dummyjsonService;

    /**
     * ---------
     * --When---
     * ---------
     */

    @When("^dummyjson > Авторизуюсь, сохраняю токен как '(.+)' и ответ как '(.+)':$")
    public void login(String tokenName, String responseName, Map<String, String> params) {
        String uri = "/auth/login";
        String body = commonUtils.getEnrichedTemplate("dummyjson/login", params);

        AtHttpRequest request = serviceUtils.generateHttpRequest(dummyjsonService)
                .resolveUri(uri)
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.toJson(jsonUtils.unify(body)));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Неверный код ответа при авторизации")
                .isEqualTo(200);

        String token = jsonUtils.getAsStringByPath(response.getBody(), "$.accessToken");
        AutoTestContext.put(tokenName, token);
        AutoTestContext.put(responseName, response.getBody());
        log.info("Авторизация выполнена, токен сохранён как '{}'", tokenName);
    }

    @When("^dummyjson > Получаю профиль пользователя по токену '(.+)' и сохраняю как '(.+)'$")
    public void getCurrentUser(String token, String responseName) {
        String uri = "/auth/me";

        AtHttpRequest request = serviceUtils.generateHttpRequest(dummyjsonService)
                .resolveUri(uri)
                .setMethod(HttpMethod.GET);
        request.getHeaders().put("Authorization", "Bearer " + enricher.enrich(token));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Неверный код ответа при получении профиля")
                .isEqualTo(200);

        AutoTestContext.put(responseName, response.getBody());
        log.info("Профиль пользователя сохранён как '{}'", responseName);
    }

    @When("^dummyjson > Ищу товары и сохраняю результат как '(.+)':$")
    public void searchProducts(String responseName, Map<String, String> params) {
        String uri = "/products/search";

        AtHttpRequest request = serviceUtils.generateHttpRequest(dummyjsonService)
                .resolveUri(uri)
                .setMethod(HttpMethod.GET)
                .setQueryParams(enricher.enrich(params));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Неверный код ответа при поиске товаров")
                .isEqualTo(200);

        AutoTestContext.put(responseName, response.getBody());
        log.info("Результат поиска товаров сохранён как '{}'", responseName);
    }

    @When("^dummyjson > Создаю товар и сохраняю как '(.+)':$")
    public void createProduct(String responseName, Map<String, String> params) {
        String uri = "/products/add";
        String body = commonUtils.getEnrichedTemplate("dummyjson/product", params);

        AtHttpRequest request = serviceUtils.generateHttpRequest(dummyjsonService)
                .resolveUri(uri)
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.toJson(jsonUtils.unify(body)));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Неверный код ответа при создании товара")
                .isEqualTo(201);

        AutoTestContext.put(responseName, response.getBody());
        log.info("Товар создан, id - '{}'", jsonUtils.getAsStringByPath(response.getBody(), "$.id"));
    }

    @When("^dummyjson > Удаляю товар '(.+)' и сохраняю ответ как '(.+)'$")
    public void deleteProduct(String productId, String responseName) {
        String uri = enricher.enrich(String.format("/products/%s", productId));

        AtHttpRequest request = serviceUtils.generateHttpRequest(dummyjsonService)
                .resolveUri(uri)
                .setMethod(HttpMethod.DELETE);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Неверный код ответа при удалении товара")
                .isEqualTo(200);

        AutoTestContext.put(responseName, response.getBody());
        log.info("Товар '{}' удалён", productId);
    }

    /**
     * ---------
     * --Then---
     * ---------
     */

    @Then("^dummyjson > Проверяю, что товар '(.+)' не найден$")
    public void checkProductNotFound(String productId) {
        String uri = enricher.enrich(String.format("/products/%s", productId));

        AtHttpRequest request = serviceUtils.generateHttpRequest(dummyjsonService)
                .resolveUri(uri)
                .setMethod(HttpMethod.GET);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        Assertions.assertThat(response.getStatusCode())
                .withFailMessage("Товар '%s' найден, ожидался код 404, получен %s", productId, response.getStatusCode())
                .isEqualTo(404);

        log.info("Товар '{}' не найден, как и ожидалось", productId);
    }
}
