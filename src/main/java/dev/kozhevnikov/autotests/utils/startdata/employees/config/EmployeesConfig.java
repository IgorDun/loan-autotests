package dev.kozhevnikov.autotests.utils.startdata.employees.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Структура employees.yaml: группы (ключи команд) со списком сотрудников.
 */
@Getter
@Setter
@Accessors(chain = true)
public class EmployeesConfig {
    private Map<String, List<Employee>> employees;

    @Data
    @Accessors(chain = true)
    public static class Employee {
        /** login, name, surname, patronymic, birthday, phoneNumber */
        private Map<String, String> info;
        private String password;
        /** сотрудник партнёра: код партнёра */
        private String partyPartnerCode;
        /** сотрудник организации: externalCode организации */
        private String partyOrgExternalCode;
        private String creationChannel;
        private Set<String> roles;
        /** код партнёра → код сотрудника в системе партнёра */
        private Map<String, String> accreditations;
        /** externalCode организаций, где сотрудник оформляет продажи */
        private Set<String> linkSeller;
        /** externalCode организаций, за которые сотрудник отвечает как менеджер */
        private Set<String> linkManager;
    }
}
