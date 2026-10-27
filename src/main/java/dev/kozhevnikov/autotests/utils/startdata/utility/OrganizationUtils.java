package dev.kozhevnikov.autotests.utils.startdata.utility;

import java.util.Map;
import java.util.Optional;

public interface OrganizationUtils {
    Optional<String> findOrganizationIdByExternalCode(String externalCode);

    String createOrganizationAndGetId(Map<String, String> organization);

    String updateOrganizationAndGetId(String orgId, Map<String, String> organization);
}
