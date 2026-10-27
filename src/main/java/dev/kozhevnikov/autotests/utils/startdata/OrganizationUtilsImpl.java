package dev.kozhevnikov.autotests.utils.startdata;

import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.CommonJsonPaths;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.OrganizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Организации в сервисе customer. Ключ поиска — externalCode: он задаётся в YAML и не меняется,
 * поэтому повторный прогон находит ранее созданную организацию и обновляет её.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class OrganizationUtilsImpl implements OrganizationUtils {
    private final ServiceUtils serviceUtils;
    private final JsonUtils jsonUtils;
    private final ServiceConfig customerService;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    @Override
    public Optional<String> findOrganizationIdByExternalCode(String externalCode) {
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/searchOrganizations"),
                Map.of("externalCode", externalCode));
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("organizations/search-by-filter")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);

        // Поиск в сервисе нечёткий, поэтому точное совпадение кода проверяется здесь
        List<Map<String, String>> organizations = jsonUtils.getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA);
        return organizations.stream()
                .filter(organization -> externalCode.equals(organization.get("externalCode")))
                .findFirst()
                .map(organization -> organization.get("id"));
    }

    @Override
    public String createOrganizationAndGetId(Map<String, String> organization) {
        return saveOrganizationAndGetId(organization);
    }

    @Override
    public String updateOrganizationAndGetId(String orgId, Map<String, String> organization) {
        organization.put("id", orgId);
        return saveOrganizationAndGetId(organization);
    }

    private String saveOrganizationAndGetId(Map<String, String> organization) {
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/postOrganization"), organization);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("organizations")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        try {
            return jsonUtils.getAsStringByPath(response.getBody(), CommonJsonPaths.DATA_ID);
        } catch (Exception e) {
            throw new AutotestException(String.format("Не удалось получить id организации %s:%n%s", organization, e.getMessage()));
        }
    }
}
