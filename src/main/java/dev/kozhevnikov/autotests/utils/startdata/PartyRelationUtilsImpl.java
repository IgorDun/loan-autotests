package dev.kozhevnikov.autotests.utils.startdata;

import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.CommonJsonPaths;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Accreditation;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartnerUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartyRelationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Связи между участниками (party) в сервисе customer: master → slave с типом связи.
 * <ul>
 *   <li>POINT_ACCREDITATION — партнёр → организация, с кодом организации у партнёра;</li>
 *   <li>ORG_STRUCT — родительская → дочерняя организация;</li>
 *   <li>EMP_ACCREDITATION — партнёр → сотрудник, с кодом сотрудника у партнёра;</li>
 *   <li>ORG_EMP, RESPONSIBLE_MANAGER — организация → сотрудник (продавец или менеджер).</li>
 * </ul>
 * Связи пересоздаются целиком: старые удаляются, затем ставятся описанные в YAML.
 * Так состояние на стенде всегда совпадает с конфигурацией, даже если YAML изменили.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class PartyRelationUtilsImpl implements PartyRelationUtils {
    private static final String POINT_ACCREDITATION = "POINT_ACCREDITATION";
    private static final String ORG_STRUCT = "ORG_STRUCT";
    private static final String EMP_ACCREDITATION = "EMP_ACCREDITATION";

    private final ServiceUtils serviceUtils;
    private final JsonUtils jsonUtils;
    private final ServiceConfig customerService;
    private final PartnerUtils partnerUtils;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    @Override
    public void deleteOrgAccreditations(String orgId) {
        findRelations(null, orgId, POINT_ACCREDITATION).forEach(this::deleteRelation);
    }

    @Override
    public void setOrgAccreditation(String orgId, Accreditation accreditation) {
        Map<String, String> relation = new HashMap<>();
        relation.put("masterPartyId", partnerUtils.findPartnerIdByCode(accreditation.getPartner()));
        relation.put("slavePartyId", orgId);
        relation.put("relationTypeCode", POINT_ACCREDITATION);
        relation.put("extCode", accreditation.getExtCode());
        relation.put("accreditationType", Optional.ofNullable(accreditation.getType()).map(String::toUpperCase).orElse(null));
        relation.put("goodsDelivery", Optional.ofNullable(accreditation.getGoodsDelivery()).map(String::toUpperCase).orElse(null));
        relation.put("state", "YES");

        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/postAccreditation"), relation);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("accreditations")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        serviceUtils.sendHttpRequest(request);
    }

    @Override
    public void deleteOrgStructRelations(String masterOrgId) {
        findRelations(masterOrgId, null, ORG_STRUCT).forEach(this::deleteRelation);
    }

    @Override
    public void setOrgStructRelation(String masterOrgId, String slaveOrgId) {
        saveRelation(relation(masterOrgId, slaveOrgId, ORG_STRUCT));
    }

    @Override
    public void deleteEmpAccreditations(String employeeId) {
        findRelations(null, employeeId, EMP_ACCREDITATION).forEach(this::deleteRelation);
    }

    @Override
    public void setEmpAccreditation(String employeeId, String partnerId, String extCode) {
        Map<String, String> relation = relation(partnerId, employeeId, EMP_ACCREDITATION);
        relation.put("extCode", extCode);
        relation.put("state", "YES");
        saveRelation(relation);
    }

    @Override
    public void deleteEmpRelations(String employeeId, String relationCode) {
        findRelations(null, employeeId, relationCode).forEach(this::deleteRelation);
    }

    @Override
    public void setEmpRelation(String employeeId, String orgId, String relationCode) {
        saveRelation(relation(orgId, employeeId, relationCode));
    }

    private Map<String, String> relation(String masterPartyId, String slavePartyId, String relationCode) {
        Map<String, String> relation = new HashMap<>();
        relation.put("masterPartyId", masterPartyId);
        relation.put("slavePartyId", slavePartyId);
        relation.put("relationTypeCode", relationCode);
        return relation;
    }

    private List<String> findRelations(String masterPartyId, String slavePartyId, String relationCode) {
        Map<String, String> filter = relation(masterPartyId, slavePartyId, relationCode);
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/searchRelations"), filter);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("relations/search")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.<List<Map<String, Object>>>findAsTypeByPath(response.getBody(), CommonJsonPaths.DATA)
                .orElse(Collections.emptyList())
                .stream()
                .filter(found -> found.containsKey("id"))
                .map(found -> String.valueOf(found.get("id")))
                .toList();
    }

    private void saveRelation(Map<String, String> relation) {
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/postRelation"), relation);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("relations")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        serviceUtils.sendHttpRequest(request);
    }

    private void deleteRelation(String relationId) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("relations/" + relationId)
                .setMethod(HttpMethod.DELETE);
        serviceUtils.sendHttpRequest(request);
    }
}
