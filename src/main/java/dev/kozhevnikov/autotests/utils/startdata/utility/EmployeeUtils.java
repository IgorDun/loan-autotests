package dev.kozhevnikov.autotests.utils.startdata.utility;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface EmployeeUtils {
    Optional<String> findEmployeeIdByLogin(String login);

    String createEmployeeAndGetId(Map<String, String> employeeData);

    String updateEmployeeAndGetId(String employeeId, Map<String, String> employeeData);

    boolean needResetPassword(String login);

    void resetPassword(String login, String password);

    void setParty(String login, String partyId);

    void setEmployeeRoles(String login, Set<String> roles);

    void setCreationChannel(String login, String creationChannel);
}
