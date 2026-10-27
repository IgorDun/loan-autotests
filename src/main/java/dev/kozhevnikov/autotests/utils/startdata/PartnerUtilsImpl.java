package dev.kozhevnikov.autotests.utils.startdata;

import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.CommonJsonPaths;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartnerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Партнёры в сервисе customer: поиск по коду и по ИНН/КПП, создание и обновление.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class PartnerUtilsImpl implements PartnerUtils {
    private final ServiceConfig customerService;
    private final JsonUtils jsonUtils;
    private final ServiceUtils serviceUtils;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    @Override
    public String findPartnerIdByCode(String partnerCode) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri(String.format("partners/by-code/%s", partnerCode.toUpperCase()))
                .setMethod(HttpMethod.GET);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_ID);
    }

    @Override
    public Optional<String> findPartnerIdByInnAndKpp(String inn, String kpp) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("partners")
                .setMethod(HttpMethod.GET)
                .setQueryParams(Map.of("inn", inn, "kpp", kpp));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.findAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_0_ID);
    }

    @Override
    public String createPartnerAndGetId(Map<String, String> partnerInfo) {
        return savePartnerAndGetId(partnerInfo);
    }

    @Override
    public String updatePartnerAndGetId(String partnerId, Map<String, String> partnerInfo) {
        partnerInfo.put("id", partnerId);
        return savePartnerAndGetId(partnerInfo);
    }

    private String savePartnerAndGetId(Map<String, String> partnerInfo) {
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/postPartner"), partnerInfo);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("partners")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_ID);
    }
}
