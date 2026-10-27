package dev.kozhevnikov.autotests.steps.api;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.utils.CucumberUtils;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.CommonUtils;
import dev.kozhevnikov.autotests.wiremock.CreateMappingRequest;
import dev.kozhevnikov.autotests.wiremock.RequestMapping;
import dev.kozhevnikov.autotests.wiremock.ResponseMapping;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class WiremockApiSteps {
    private final CommonUtils commonUtils;
    private final JsonUtils jsonUtils;
    private final CucumberUtils cucumberUtils;
    private final ServiceUtils serviceUtils;
    private final ServiceConfig wiremockService;

    @When("wiremock > Создаю заглушку:")
    @SuppressWarnings("unchecked")
    public void createStab(DataTable table) {
        List<List<String>> params = commonUtils.enrichLists(table.asLists());

        RequestMapping requestMapping = new RequestMapping();
        cucumberUtils.filterRows(params, row -> "requestMethod".equals(row.get(0)))
                .forEach(param -> requestMapping.setMethod(param.get(1)));
        cucumberUtils.filterRows(params, row -> "requestUrlPattern".equals(row.get(0)))
                .forEach(param -> requestMapping.setUrlPattern(param.get(1)));
        cucumberUtils.filterRows(params, row -> "requestBodyPattern".equals(row.get(0)))
                .forEach(param -> requestMapping.setBodyPatterns(List.of(new AbstractMap.SimpleEntry<>(param.get(1), param.get(2)))));

        ResponseMapping responseMapping = new ResponseMapping();
        cucumberUtils.filterRows(params, row -> "responseStatus".equals(row.get(0)))
                .forEach(param -> responseMapping.setStatus(Integer.valueOf(param.get(1))));
        cucumberUtils.filterRows(params, row -> "responseBody".equals(row.get(0)))
                .forEach(param -> responseMapping.setBody(param.get(1)));
        cucumberUtils.filterRows(params, row -> "responseHeader".equals(row.get(0)))
                .forEach(param -> responseMapping.setHeaders(Map.of(param.get(1), param.get(2))));

        CreateMappingRequest createMappingRequest = new CreateMappingRequest();
        createMappingRequest.setPriority(1);
        createMappingRequest.setRequest(requestMapping);
        createMappingRequest.setResponse(responseMapping);

        AtHttpRequest request = serviceUtils.generateHttpRequest(wiremockService)
                .setMethod(HttpMethod.POST)
                .setBody(createMappingRequest);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);

        Object responseBody = Objects.requireNonNull(response.getBody(), "Пустое тело ответа от wiremock");
        String mappingId = jsonUtils.getAsStringByPath(responseBody, "$.id");
        log.info("Создана заглушка, id - {}", mappingId);
        if (AutoTestContext.get("mappings") == null) {
            AutoTestContext.put("mappings", List.of());
        }
        ((List<String>) AutoTestContext.get("mappings")).add(mappingId);
    }
}