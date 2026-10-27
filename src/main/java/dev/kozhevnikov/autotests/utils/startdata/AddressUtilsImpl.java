package dev.kozhevnikov.autotests.utils.startdata;

import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.api.model.http.AtHttpResponse;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.CommonJsonPaths;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Address;
import dev.kozhevnikov.autotests.utils.startdata.utility.AddressUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Адреса организаций в сервисе customer. Адрес — отдельная сущность, общая для организаций:
 * сначала ищется по строке адреса и создаётся, только если не найден.
 */
@Component
@Lazy
@RequiredArgsConstructor
public class AddressUtilsImpl implements AddressUtils {
    private final ServiceConfig customerService;
    private final JsonUtils jsonUtils;
    private final ServiceUtils serviceUtils;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    @Override
    public void deleteOrganizationAddresses(String organizationId) {
        AtHttpRequest getRequest = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri(String.format("parties/%s/addresses", organizationId))
                .setMethod(HttpMethod.GET);
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(getRequest);
        jsonUtils.<List<Map<String, Object>>>getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA)
                .stream()
                .filter(address -> address.containsKey("id"))
                .map(address -> address.get("id"))
                .forEach(addressId -> {
                    AtHttpRequest deleteRequest = serviceUtils.getRequestForGenerate(customerService)
                            .resolveUri(String.format("parties/%s/addresses/%s", organizationId, addressId))
                            .setMethod(HttpMethod.DELETE);
                    serviceUtils.sendHttpRequest(deleteRequest);
                });
    }

    @Override
    public void setAddress(String organizationId, String addressId) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri(String.format("parties/%s/addresses/%s", organizationId, addressId))
                .setMethod(HttpMethod.POST);
        serviceUtils.sendHttpRequest(request);
    }

    @Override
    public Optional<String> findAddress(Address address) {
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("addresses")
                .setMethod(HttpMethod.GET)
                .setQueryParams(serviceUtils.createQueryParam("address", address.getInline()));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.findAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_0_ID);
    }

    @Override
    public String createAddressAndGetId(Address address) {
        Map<String, String> params = new HashMap<>();
        params.put("addressTypeCode", address.getType());
        params.put("address", address.getInline());
        Optional.ofNullable(address.getRegionCode()).ifPresent(regionCode -> params.put("regionCode", regionCode));
        String body = enricher.enrich(fileUtils.readMainResourceTemplate("customer/postAddress"), params);
        AtHttpRequest request = serviceUtils.getRequestForGenerate(customerService)
                .resolveUri("addresses")
                .setMethod(HttpMethod.POST)
                .setBody(jsonUtils.unify(body));
        AtHttpResponse<Object> response = serviceUtils.sendHttpRequest(request);
        return jsonUtils.getAsTypeByPath(response.getBody(), CommonJsonPaths.DATA_ID);
    }
}
