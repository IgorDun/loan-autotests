package dev.kozhevnikov.autotests.utils.startdata.utility;

import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Accreditation;

public interface PartyRelationUtils {
    void deleteOrgAccreditations(String orgId);

    void setOrgAccreditation(String orgId, Accreditation accreditation);

    void deleteOrgStructRelations(String masterOrgId);

    void setOrgStructRelation(String masterOrgId, String slaveOrgId);

    void deleteEmpAccreditations(String employeeId);

    void setEmpAccreditation(String employeeId, String partnerId, String extCode);

    void deleteEmpRelations(String employeeId, String relationCode);

    void setEmpRelation(String employeeId, String orgId, String relationCode);
}
