package dev.kozhevnikov.autotests.steps.front;

import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionFactory;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v155.fetch.Fetch;
import org.openqa.selenium.devtools.v155.fetch.model.HeaderEntry;
import org.openqa.selenium.devtools.v155.fetch.model.RequestId;
import org.openqa.selenium.devtools.v155.network.Network;
import org.openqa.selenium.devtools.v155.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v155.network.model.ResponseReceived;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.WaitUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Optional.empty;


@Slf4j
@RequiredArgsConstructor
public class DevToolsSteps {
    private final Enricher enricher;
    private final JsonUtils jsonUtils;
    private final WaitUtils waitUtils;
    private final AbstractFrontSteps abstractFrontSteps;
    private final EnvSettings envSettings;
    private Object bodyForModified;
    private String methodForModified;
    private boolean fetchListenerIsActive;

    /**
     * --------
     * --When--
     * --------
     */

    @When("^devTools > Сохраняю токен авторизации$")
    public void getToken() {
        getWaitSettings(envSettings.getDevToolsTimeoutInSeconds(), envSettings.getDevToolsFastIntervalInMilliseconds())
                .until(() -> Optional.ofNullable(abstractFrontSteps.getAtDevTools().getLastRequest())
                        .map(request -> request.getRequest().getHeaders())
                        .map(header -> header.get("authorization"))
                        .map(authToken -> {
                            String token = authToken.toString().replaceAll("Bearer ", "");
                            AutoTestContext.put("bearerToken", token);
                            log.info("Сохранен токен авторизации: {}", token);
                            return true;
                        })
                        .orElse(false));
    }

    @When("^devTools > Ожидаю доступного действия:$")
    public void checkActionDevTools(Map<String, String> params) {
        Map<String, String> enrichedParams = enricher.enrich(params);
        getWaitSettings(envSettings.getDevToolsLongTimeoutInSeconds(), envSettings.getDevToolsIntervalInMilliseconds())
                .until(() -> {
                    ResponseReceived responseReceived = abstractFrontSteps.getAtDevTools().getLastResponse();
                    String url = responseReceived.getResponse().getUrl();
                    if (url.contains("state") || url.contains("decisions-info")) {
                        Object responseBody = abstractFrontSteps.getAtDevTools()
                                .send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
                        log.info("Ответ: " + responseBody.toString());
                        boolean someCondition = responseReceived.getResponse().getUrl().contains("state");
                        String path = someCondition ? "$.actions" : "$.availableActions";
                        return enrichedParams.entrySet().stream()
                                .allMatch(entry -> jsonUtils.getAsStringByPath(responseBody, path)
                                        .contains(entry.getKey()) == Boolean.parseBoolean(entry.getValue()));
                    } else {
                        return false;
                    }
                });
    }

    @When("^devTools > Ожидаю в ответе метода '(.*)':$")
    public void waitUntilDevTools(String method, Map<String, String> params) {
        Map<String, String> enrichedParams = enricher.enrich(params);
        getWaitSettings(envSettings.getDevToolsLongTimeoutInSeconds(), envSettings.getDevToolsIntervalInMilliseconds())
                .until(() -> {
                    ResponseReceived responseReceived = abstractFrontSteps.getAtDevTools().getLastResponse();
                    if (responseReceived.getResponse().getUrl().contains(method)) {
                        Object responseBody = abstractFrontSteps.getAtDevTools()
                                .send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
                        log.info("Ответ: " + responseBody.toString());
                        return enrichedParams.entrySet().stream()
                                .allMatch(entry -> jsonUtils
                                        .checkValue(responseBody, entry.getKey(), entry.getValue()));
                    } else {
                        return false;
                    }
                });
    }

    @When("^devTools > Сохраняю тело запроса '(.*)' как '(.*)'$")
    public void saveRequestBody(String method, String name) {
        getWaitSettings(envSettings.getDevToolsTimeoutInSeconds(), envSettings.getDevToolsIntervalInMilliseconds())
                .until(() -> {
                    RequestWillBeSent requestWillBeSent = abstractFrontSteps.getAtDevTools().getLastRequest();
                    if (requestWillBeSent.getRequest().getUrl().contains(enricher.enrich(method))) {
                        String bodyForSave = requestWillBeSent.getRequest().getPostData().orElse("");
                        AutoTestContext.put(name, bodyForSave);
                        log.info("Тело запроса '{}' сохранено как '{}'", bodyForSave, name);
                        return true;
                    } else {
                        return false;
                    }
                });
    }

    @When("^devTools > Сохраняю тело ответа '(.*)' как '(.*)'$")
    public void saveResponseBody(String method, String name) {
        String enrichedMethod = enricher.enrich(method);
        log.info("Искомый метод после обогащения: /{}", enrichedMethod);

        getWaitSettings(envSettings.getDevToolsTimeoutInSeconds(), envSettings.getDevToolsFastIntervalInMilliseconds())
                .until(() -> {
                    Map<LocalDateTime, ResponseReceived> responses = new LinkedHashMap<>(abstractFrontSteps.getAtDevTools().getAllResponses());

                    for (Map.Entry<LocalDateTime, ResponseReceived> entry : responses.entrySet()) {
                        ResponseReceived responseReceived = entry.getValue();

                        if (responseReceived != null && responseReceived.getResponse() != null) {
                            String url = responseReceived.getResponse().getUrl();
                            log.info("Обрабатываемый URL: {}", url);

                            if (!url.isEmpty() && !enrichedMethod.isEmpty() && url.contains("/" + enrichedMethod)) {
                                try {
                                    String bodyForSave = abstractFrontSteps.getAtDevTools()
                                            .send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
                                    if (bodyForSave != null && !bodyForSave.isEmpty()) {
                                        AutoTestContext.put(name, bodyForSave);
                                        log.info("Тело ответа '{}' сохранено как '{}'", bodyForSave, name);
                                        return true;
                                    } else {
                                        log.error("Тело ответа пустое или null");
                                    }
                                } catch (DevToolsException e) {
                                    log.info("Ошибка -3200 будет повторный вызов");
                                }
                            } else {
                                log.error("URL ответа не содержит метод '/{}'", enrichedMethod);
                            }
                        } else {
                            log.error("Получен пустой или null ответ или запрос");
                        }
                    }
                    return false;
                });
    }

    @When("^devTools > Меняю тело ответа '(.*)' на '(.*)'$")
    public void changeResponseBody(String method, String name) {
        methodForModified = method;
        bodyForModified = AutoTestContext.get(name);
        setResponseBody();
    }

    @When("^devTools > Меняю параметры в теле ответа '(.*)':$")
    public void changeResponseBody(String method, Map<String, String> params) {
        Map<String, String> enrichedParams = enricher.enrich(params);
        getWaitSettings(envSettings.getDevToolsTimeoutInSeconds(), envSettings.getDevToolsIntervalInMilliseconds())
                .until(() -> {
                    ResponseReceived responseReceived = abstractFrontSteps.getAtDevTools().getLastResponse();
                    if (responseReceived.getResponse().getUrl().contains(method)) {
                        Object responseBody = abstractFrontSteps.getAtDevTools()
                                .send(Network.getResponseBody(responseReceived.getRequestId())).getBody();
                        Object modifiedResponseBody = jsonUtils.toJson(responseBody);
                        enrichedParams
                                .forEach((key, value) -> jsonUtils.setValue(modifiedResponseBody, key, value));
                        methodForModified = method;
                        bodyForModified = modifiedResponseBody.toString();
                        setResponseBody();
                        return true;
                    } else {
                        return false;
                    }
                });
    }

    @When("^devTools > Закрываю сессию Fetch$")
    public void closeFetch() {
        abstractFrontSteps.getAtDevTools().send(Fetch.disable());
        fetchListenerIsActive = false;
        log.info("Сессия Fetch закрыта");
    }

    private ConditionFactory getWaitSettings(long secondsTimeout, long millisecondsPulling) {
        return waitUtils.getDefaultSettings()
                .timeout(secondsTimeout, TimeUnit.SECONDS)
                .pollInterval(millisecondsPulling, TimeUnit.MILLISECONDS);
    }


    private void setResponseBody() {
        if (!fetchListenerIsActive) {
            abstractFrontSteps.getAtDevTools().send(Fetch.enable(empty(), empty()));
            ResponseReceived responseReceived = abstractFrontSteps.getAtDevTools().getLastResponse();
            Map<String, Object> headers = responseReceived.getResponse().getHeaders().toJson();
            List<HeaderEntry> headerEntries = headers.entrySet().stream()
                    .map(entry -> new HeaderEntry(entry.getKey(), entry.getValue().toString()))
                    .collect(Collectors.toList());

            abstractFrontSteps.getAtDevTools().addListener(Fetch.requestPaused(), request -> {
                fetchListenerIsActive = true;
                RequestId requestId = request.getRequestId();
                String url = request.getRequest().getUrl();
                if (url.contains(methodForModified) && !url.contains("yandex")) {
                    abstractFrontSteps.getAtDevTools().send(Fetch.fulfillRequest(
                            requestId,
                            request.getResponseStatusCode().orElse(200),
                            Optional.of(headerEntries),
                            empty(),
                            Optional.of(Base64.getEncoder()
                                    .encodeToString(bodyForModified.toString().getBytes())),
                            empty()));
                    log.info("Тело ответа '{}' было изменено на '{}'", url, bodyForModified);
                } else {
                    abstractFrontSteps.getAtDevTools()
                            .send(Fetch.continueRequest(requestId, empty(), empty(), empty(),
                                    empty(), empty()));
                }
            });
        }
    }
}