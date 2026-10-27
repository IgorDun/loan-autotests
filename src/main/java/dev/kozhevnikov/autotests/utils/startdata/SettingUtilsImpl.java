package dev.kozhevnikov.autotests.utils.startdata;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.startdata.utility.SettingUtils;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Класс для работы с настройками.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class SettingUtilsImpl implements SettingUtils {

    private final ServiceUtils serviceUtils;
    private final JsonUtils jsonUtils;
    private final ServiceConfig settingsService;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    /**
     * Получает идентификатор настройки для указанного типа субъекта настроек.
     *
     * @param settingSubjectFilterDto параметры фильтрации для настройки субъекта
     *
     * @return идентификатор настройки субъекта
     * @throws dev.kozhevnikov.autotests.exception.AutotestException в случае если запись не найдена и создается новая запись.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String getSettingSubjects(Map<String, String> settingSubjectFilterDto) {
        Map<String, Object> queryParam = new HashMap<>();
        if (settingSubjectFilterDto.get("subjectType").equals("CREATION_CHANNEL")) {
            if (settingSubjectFilterDto.get("subjectType") != null) {
                queryParam = Map.of(
                        "subjectType", settingSubjectFilterDto.get("subjectType"));
            }
            if (settingSubjectFilterDto.get("creationChannel") != null) {
                queryParam = Map.of(
                        "creationChannel", settingSubjectFilterDto.get("creationChannel"));
            }
        } else if (settingSubjectFilterDto.get("subjectType").equals("PROCESS")) {
            if (settingSubjectFilterDto.get("subjectType") != null) {
                queryParam = Map.of(
                        "subjectType", settingSubjectFilterDto.get("subjectType"));
            }
            if (settingSubjectFilterDto.get("processCode") != null) {
                queryParam = Map.of(
                        "processCode", settingSubjectFilterDto.get("processCode"));
            }
        } else if (settingSubjectFilterDto.get("subjectType").equals("RELATION")) {
            if (settingSubjectFilterDto.get("partyId") != null) {
                queryParam = Map.of(
                        "partyId", settingSubjectFilterDto.get("partyId"));
            }
            if (settingSubjectFilterDto.get("linkedProviderId") != null) {
                queryParam = Map.of(
                        "linkedProviderId", settingSubjectFilterDto.get("linkedProviderId"));
            }
        } else {
            if (settingSubjectFilterDto.get("subjectType") != null) {
                queryParam = Map.of(
                        "subjectType", settingSubjectFilterDto.get("subjectType"));
            }
            if (settingSubjectFilterDto.get("partyId") != null) {
                queryParam = Map.of(
                        "partyId", settingSubjectFilterDto.get("partyId"));
            }
        }

        AtHttpRequest getRequest = serviceUtils.getRequestForGenerate(settingsService)
                .resolveUri("setting-subjects")
                .setMethod(HttpMethod.GET)
                .setQueryParams(serviceUtils.createQueryParams(queryParam));
        AtHttpResponse<Object> getResponse = serviceUtils.sendHttpRequest(getRequest);

        try {
            String settingSubjectIdKey = "settingSubjectId";
            return ((List<Map<String, Object>>) getResponse.getBody())
                    .stream()
                    .filter(map -> map.containsKey(settingSubjectIdKey))
                    .map(map -> (String) map.get(settingSubjectIdKey))
                    .findFirst()
                    .orElseThrow(() -> new AutotestException("Не удалось получить settingSubjectId для " + settingSubjectFilterDto));
        } catch (Exception e) {
            String templatePath = "settings/createSubjectId";
            String body = enricher.enrich(fileUtils.readMainResourceTemplate(templatePath), settingSubjectFilterDto);

            AtHttpRequest postRequest = serviceUtils.getRequestForGenerate(settingsService)
                    .resolveUri("setting-subjects")
                    .setMethod(HttpMethod.POST)
                    .setBody(jsonUtils.unify(body));
            AtHttpResponse<Object> postResponse = serviceUtils.sendHttpRequest(postRequest);

            return jsonUtils.getAsTypeByPath(postResponse.getBody(), "$.settingSubjectId");
        }
    }

    /**
     * Выполняет поиск типа параметра по указанным параметрам.
     *
     * @param paramTypeFilterDto параметры фильтрации для поиска типа параметра
     * @return идентификатор найденного типа параметра, если найден, в виде Optional, в противном случае возвращает пустое значение
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> findParamTypeByParams(Map<String, String> paramTypeFilterDto) {
        String paramNames = String.join(",", paramTypeFilterDto.get("paramNames"));
        String[] queryParamKeys = {"subjectType", "dataType", "paramNames"};
        Object[] queryParamValues = {paramTypeFilterDto.get("subjectType"), paramTypeFilterDto.get("dataType"), paramNames};

        AtHttpRequest request = serviceUtils.getRequestForGenerate(settingsService)
                .resolveUri("param-types")
                .setMethod(HttpMethod.GET)
                .setQueryParams(serviceUtils.createQueryParams(queryParamKeys, queryParamValues));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);

        return ((List<Map<String, Object>>) response.getBody())
                .stream()
                .filter(map -> map.containsKey("paramTypeId"))
                .map(map -> (String) map.get("paramTypeId"))
                .findFirst();
    }

    /**
     * Создает тип параметра с указанными данными.
     *
     * @param paramTypeDto данные для создания типа параметра
     * @return идентификатор созданного типа параметра
     */
    @Override
    public String createParamType(Map<String, String> paramTypeDto) {
        String templatePath = "settings/createParamType";
        String body = enricher.enrich(fileUtils.readMainResourceTemplate(templatePath), paramTypeDto);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(settingsService)
                .resolveUri("param-types")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);

        return jsonUtils.getAsTypeByPath(response.getBody(), "$.paramTypeId");
    }

    /**
     * Выполняет поиск настроек по указанным параметрам.
     *
     * @param settingFilterDto параметры фильтрации для поиска настроек
     * @return найденные настройки в виде Optional, если найдены, иначе возвращает пустое значение
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если найдено более 1-ой активной настройки
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<Map<String, String>> findSettingsByParams(Map<String, String> settingFilterDto) {
        String paramNames = String.join(",", settingFilterDto.get("paramNames"));
        String[] queryParamKeys = {"paramNames", "partyId", "settingSubjectId", "onlyActive"};
        Object[] queryParamValues = {paramNames, settingFilterDto.get("partyId"), settingFilterDto.get("settingSubjectId"), true};

        AtHttpRequest request = serviceUtils.getRequestForGenerate(settingsService)
                .resolveUri("settings")
                .setMethod(HttpMethod.GET)
                .setQueryParams(serviceUtils.createQueryParams(queryParamKeys, queryParamValues));
        List<Map<String, String>> responseBody = (List<Map<String, String>>) serviceUtils.sendHttpRequest(request).getBody();

        if (responseBody.size() <= 1) {
            return responseBody.stream().findFirst();

        } else {
            throw new AutotestException(String.format("Ожидается не более 1 активной настройки, найдено %s:%n%s", responseBody.size(), responseBody));
        }
    }

    /**
     * Создает новую настройку с указанными данными.
     *
     * @param settingDtoRequest данные для создания настройки
     * @return идентификатор созданной настройки
     */
    @Override
    public String createSetting(Map<String, String> settingDtoRequest) {
        String templatePath = "settings/createSetting";
        String body = enricher.enrich(fileUtils.readMainResourceTemplate(templatePath), settingDtoRequest);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(settingsService)
                .resolveUri("settings")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);

        return jsonUtils.getAsTypeByPath(response.getBody(), "$.settingId");
    }

    /**
     * Обновляет указанную настройку с указанными данными.
     *
     * @param settingId идентификатор настройки
     * @param settingDtoRequest новые данные для настройки
     */
    @Override
    public void updateSetting(String settingId, Map<String, String> settingDtoRequest) {
        String templatePath = "settings/createSetting";
        String body = enricher.enrich(fileUtils.readMainResourceTemplate(templatePath), settingDtoRequest);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(settingsService)
                .resolveUri("settings/" + settingId)
                .setMethod(HttpMethod.PATCH)
                .setBody(jsonUtils.unify(body));
        serviceUtils.sendHttpRequest(request);
    }
}