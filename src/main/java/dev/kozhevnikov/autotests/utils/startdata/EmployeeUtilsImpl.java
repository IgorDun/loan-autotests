package dev.kozhevnikov.autotests.utils.startdata;

import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.CommonJsonPaths;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.EmployeeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Сотрудники: карточка в сервисе customer, учётная запись (роли, канал, пароль) в сервисе authorization.
 * Пароль сбрасывается через штатный флоу с SMS-кодом: код берётся из сервиса passcoder.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class EmployeeUtilsImpl implements EmployeeUtils {
    private final ServiceUtils serviceUtils;
    private final JsonUtils jsonUtils;
    private final ServiceConfig customerService;
    private final ServiceConfig authorizationService;
    private final ServiceConfig passcoderService;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    @Override
    public Optional<String> findEmployeeIdByLogin(String login) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("employees")
                .setMethod(HttpMethod.GET)
                .setQueryParams(Map.of("login", login));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.findAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_0_ID);
    }

    @Override
    public String createEmployeeAndGetId(Map<String, String> employeeData) {
        createClient(employeeData.get("login"));
        return saveEmployeeAndGetId(employeeData);
    }

    @Override
    public String updateEmployeeAndGetId(String employeeId, Map<String, String> employeeData) {
        employeeData.put("id", employeeId);
        return saveEmployeeAndGetId(employeeData);
    }

    @Override
    public boolean needResetPassword(String login) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri("clients/" + login)
                .setMethod(HttpMethod.GET);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.getAsTypeByPath(response.getBody(), "$.data.requiredResetSecret");
    }

    @Override
    public void resetPassword(String login, String password) {
        AtHttpResponse<Object> resetResponse = postResetPassword(login, Map.of("mobilePhone", login));
        String sessionId = jsonUtils.getAsTypeByPath(resetResponse.getBody(), CommonJsonPaths.DATA_ID);

        AtHttpRequest sessionRequest = serviceUtils.getRequestForGenerate(passcoderService)
                .resolveUri("results/sessions/" + sessionId)
                .setMethod(HttpMethod.GET);
        AtHttpResponse<Object> sessionResponse = serviceUtils.sendHttpRequest(sessionRequest);
        String passCode = jsonUtils.getAsTypeByPath(sessionResponse.getBody(), "$.passCode");

        postResetPassword(login, Map.of(
                "mobilePhone", login,
                "sessionId", sessionId,
                "passCode", passCode,
                "password", password,
                "confirmPassword", password
        ));
    }

    @Override
    public void setParty(String login, String partyId) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri("clients")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.toJson(Map.of("clientId", login, "partyId", partyId)));
        serviceUtils.sendHttpRequest(request);
    }

    @Override
    public void setEmployeeRoles(String login, Set<String> roles) {
        AtHttpRequest getRequest = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri(String.format("clients/%s/roles", login))
                .setMethod(HttpMethod.GET);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(getRequest);
        jsonUtils.<List<Map<String, Object>>>getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA)
                .stream()
                .filter(role -> role.containsKey("id"))
                .map(role -> role.get("id"))
                .forEach(roleId -> {
                    AtHttpRequest deleteRequest = serviceUtils.getRequestForGenerate(authorizationService)
                            .resolveUri(String.format("clients/%s/roles/%s", login, roleId))
                            .setMethod(HttpMethod.DELETE);
                    serviceUtils.sendHttpRequest(deleteRequest);
                });

        AtHttpRequest postRequest = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri(String.format("clients/%s/roles", login))
                .setMethod(HttpMethod.POST)
                .setBody(roles);
        serviceUtils.sendHttpRequest(postRequest);
    }

    @Override
    public void setCreationChannel(String login, String creationChannel) {
        AtHttpRequest deleteRequest = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri("channels/" + login)
                .setMethod(HttpMethod.DELETE);
        serviceUtils.sendHttpRequest(deleteRequest);

        AtHttpRequest postRequest = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri("channels")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.toJson(Map.of("clientId", login, "channelCode", creationChannel)));
        serviceUtils.sendHttpRequest(postRequest);
    }

    private String saveEmployeeAndGetId(Map<String, String> employeeData) {
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/postEmployee"), employeeData);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("employees")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_ID);
    }

    private void createClient(String login) {
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("authorization/postClient"), Map.of("clientId", login));
        AtHttpRequest request = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri("clients")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        serviceUtils.sendHttpRequest(request);
    }

    private AtHttpResponse<Object> postResetPassword(String login, Map<String, String> body) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(authorizationService)
                .resolveUri(String.format("clients/%s/passwords/reset", login))
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.toJson(body));
        return serviceUtils.sendHttpRequest(request);
    }
}
