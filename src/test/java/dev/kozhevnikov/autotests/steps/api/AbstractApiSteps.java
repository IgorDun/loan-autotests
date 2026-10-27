package dev.kozhevnikov.autotests.steps.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.config.CucumberConfiguration;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.common.WaitUtils;

import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Lazy
public class AbstractApiSteps extends CucumberConfiguration {
    private final FileUtils fileUtils;
    private final JsonUtils jsonUtils;
    private final Enricher enricher;
    private final ServiceUtils serviceUtils;
    private final WaitUtils waitUtils;

    public String toBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public String fileToBase64(String fileName) {
        File file = fileUtils.getResourceFile(fileName);
        return fileUtils.toBase64(file);
    }

    public String fromBase64(String base64str) {
        byte[] bytes = Base64.getDecoder().decode(base64str);
        return new String(bytes);
    }

    public boolean isNull(Object obj) {
        return obj == null || obj.equals("null");
    }

    public List<List<String>> enrichLists(List<List<String>> params) {
        return params.stream()
                .map(enricher::enrich)
                .collect(Collectors.toList());
    }

    public String getJsonByPathForListMap(String templatePath, List<Map<String, String>> params) {
        return params.stream().map(object -> enricher.enrich(fileUtils.readResourceTemplate(templatePath), enricher.enrich(object)))
                .collect(Collectors.toList()).toString();
    }

    public AtHttpRequest getRequest(ServiceConfig service, String uri) {
        String enrichedUri = enricher.enrich(uri);

        return serviceUtils.generateHttpRequest(service)
                .resolveUri(enrichedUri)
                .setMethod(HttpMethod.GET);
    }

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

    public String getEnrichedTemplate(String templateName, Map<String, String> map) {
        String template = fileUtils.readResourceTemplate(templateName);
        Map<String, String> enrichedMap = enricher.enrich(map);

        return enricher.enrich(template, enrichedMap);
    }

    public List<Object> createListFromTemplate(String template, List<Map<String, String>> params) {
        return params.stream()
                .map(enricher::enrich)
                .map(object -> enricher.enrich(fileUtils.readResourceTemplate(template), object))
                .map(jsonUtils::unify)
                .map(object -> jsonUtils.fromJson(object, Object.class))
                .collect(Collectors.toList());
    }
}