package dev.kozhevnikov.autotests.utils.startdata.organizations.factory.impl;

import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.startdata.organizations.collector.OrganizationsCollector;
import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Organization;
import dev.kozhevnikov.autotests.utils.startdata.organizations.factory.OrganizationsFactory;
import dev.kozhevnikov.autotests.utils.startdata.utility.AddressUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.OrganizationUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartyRelationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Строит дерево организаций снизу вверх: точка продаж → группа точек → офис → группа офисов → сеть → группа сетей.
 * Родитель ссылается на дочерние организации по externalCode, поэтому каждый уровень создаётся
 * только после предыдущего. Внутри уровня организации независимы и создаются параллельно.
 * <p>
 * Для каждой организации: find-or-create-or-update по externalCode, затем адреса, аккредитации
 * у партнёров и связи с дочерними организациями пересоздаются по YAML.
 */
@Component
@RequiredArgsConstructor
public class OrganizationsFactoryImpl implements OrganizationsFactory {
    private static final List<String> CATEGORIES_BOTTOM_UP = List.of(
            "POINT", "POINT_GROUP", "OFFICE", "OFFICE_GROUP", "NETWORK", "NETWORK_GROUP");

    private final OrganizationsCollector organizationsCollector;
    private final OrganizationUtils organizationUtils;
    private final AddressUtils addressUtils;
    private final PartyRelationUtils partyRelationUtils;

    @Override
    public List<Organization> create() {
        List<Organization> organizations = organizationsCollector.collect();
        List<Organization> result = new ArrayList<>();
        CATEGORIES_BOTTOM_UP.forEach(category -> result.addAll(create(organizations, category)));
        return result;
    }

    private List<Organization> create(List<Organization> organizations, String category) {
        return organizations.parallelStream()
                .filter(organization -> category.equals(organization.getInfo().get("category")))
                .map(this::createOrUpdateOrganization)
                .map(this::setAddresses)
                .map(this::setAccreditations)
                .map(this::setChildOrganizations)
                .toList();
    }

    private Organization createOrUpdateOrganization(Organization organization) {
        String externalCode = organization.getInfo().get("externalCode");
        String id = organizationUtils.findOrganizationIdByExternalCode(externalCode)
                .map(orgId -> organizationUtils.updateOrganizationAndGetId(orgId, organization.getInfo()))
                .orElseGet(() -> organizationUtils.createOrganizationAndGetId(organization.getInfo()));
        organization.getInfo().put("id", id);
        return organization;
    }

    private Organization setAddresses(Organization organization) {
        String orgId = organization.getInfo().get("id");
        addressUtils.deleteOrganizationAddresses(orgId);
        Optional.ofNullable(organization.getAddresses())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(address -> addressUtils.findAddress(address)
                        .orElseGet(() -> addressUtils.createAddressAndGetId(address)))
                .forEach(addressId -> addressUtils.setAddress(orgId, addressId));
        return organization;
    }

    private Organization setAccreditations(Organization organization) {
        String orgId = organization.getInfo().get("id");
        partyRelationUtils.deleteOrgAccreditations(orgId);
        Optional.ofNullable(organization.getAccreditations())
                .orElseGet(Collections::emptyList)
                .forEach(accreditation -> partyRelationUtils.setOrgAccreditation(orgId, accreditation));
        return organization;
    }

    private Organization setChildOrganizations(Organization organization) {
        String orgId = organization.getInfo().get("id");
        partyRelationUtils.deleteOrgStructRelations(orgId);
        Optional.ofNullable(organization.getChildOrgExternalCodes())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(childCode -> organizationUtils.findOrganizationIdByExternalCode(childCode)
                        .orElseThrow(() -> new AutotestException(String.format(
                                "Дочерняя организация '%s' не найдена: она должна быть описана уровнем ниже", childCode))))
                .forEach(childId -> partyRelationUtils.setOrgStructRelation(orgId, childId));
        return organization;
    }
}
