package dev.kozhevnikov.autotests.utils.startdata.utility;

import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Address;

import java.util.Optional;

public interface AddressUtils {
    void deleteOrganizationAddresses(String organizationId);

    void setAddress(String organizationId, String addressId);

    Optional<String> findAddress(Address address);

    String createAddressAndGetId(Address address);
}
