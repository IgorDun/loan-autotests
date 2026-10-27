package dev.kozhevnikov.autotests.utils.startdata.employees.factory.impl;

import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.startdata.employees.collector.EmployeesCollector;
import dev.kozhevnikov.autotests.utils.startdata.employees.config.EmployeesConfig.Employee;
import dev.kozhevnikov.autotests.utils.startdata.employees.factory.EmployeesFactory;
import dev.kozhevnikov.autotests.utils.startdata.utility.EmployeeUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.OrganizationUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartnerUtils;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartyRelationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Сотрудник ищется по логину: найден — обновляется, не найден — создаётся вместе с учётной записью.
 * Затем: сброс пароля (если требуется), привязка к организации или партнёру, роли, канал,
 * аккредитации у партнёров и связи с организациями как продавца и как ответственного менеджера.
 * Запускается после организаций и партнёров: ссылается на них по externalCode и коду партнёра.
 */
@Component
@RequiredArgsConstructor
public class EmployeesFactoryImpl implements EmployeesFactory {
    private static final String ID = "id";
    private static final String LOGIN = "login";
    private static final String SELLER = "ORG_EMP";
    private static final String MANAGER = "RESPONSIBLE_MANAGER";

    private final EmployeesCollector employeesCollector;
    private final EmployeeUtils employeeUtils;
    private final PartnerUtils partnerUtils;
    private final OrganizationUtils organizationUtils;
    private final PartyRelationUtils partyRelationUtils;

    @Override
    public List<Employee> create() {
        return employeesCollector.collect()
                .parallelStream()
                .map(this::createOrUpdate)
                .map(this::setParty)
                .map(this::setRoles)
                .map(this::setCreationChannel)
                .map(this::setAccreditations)
                .map(employee -> setOrgRelations(employee, employee.getLinkSeller(), SELLER))
                .map(employee -> setOrgRelations(employee, employee.getLinkManager(), MANAGER))
                .toList();
    }

    private Employee createOrUpdate(Employee employee) {
        String login = employee.getInfo().get(LOGIN);
        String id = employeeUtils.findEmployeeIdByLogin(login)
                .map(employeeId -> employeeUtils.updateEmployeeAndGetId(employeeId, employee.getInfo()))
                .orElseGet(() -> employeeUtils.createEmployeeAndGetId(employee.getInfo()));
        employee.getInfo().put(ID, id);

        if (employee.getPassword() != null && employeeUtils.needResetPassword(login)) {
            employeeUtils.resetPassword(login, employee.getPassword());
        }
        return employee;
    }

    private Employee setParty(Employee employee) {
        employeeUtils.setParty(employee.getInfo().get(LOGIN), getPartyId(employee));
        return employee;
    }

    private Employee setRoles(Employee employee) {
        Optional.ofNullable(employee.getRoles())
                .ifPresent(roles -> employeeUtils.setEmployeeRoles(employee.getInfo().get(LOGIN), roles));
        return employee;
    }

    private Employee setCreationChannel(Employee employee) {
        Optional.ofNullable(employee.getCreationChannel())
                .ifPresent(channel -> employeeUtils.setCreationChannel(employee.getInfo().get(LOGIN), channel));
        return employee;
    }

    private Employee setAccreditations(Employee employee) {
        String employeeId = employee.getInfo().get(ID);
        partyRelationUtils.deleteEmpAccreditations(employeeId);
        Optional.ofNullable(employee.getAccreditations())
                .map(Map::entrySet)
                .orElseGet(Collections::emptySet)
                .forEach(entry -> partyRelationUtils.setEmpAccreditation(
                        employeeId, partnerUtils.findPartnerIdByCode(entry.getKey()), entry.getValue()));
        return employee;
    }

    private Employee setOrgRelations(Employee employee, Set<String> orgExternalCodes, String relationCode) {
        String employeeId = employee.getInfo().get(ID);
        partyRelationUtils.deleteEmpRelations(employeeId, relationCode);
        Optional.ofNullable(orgExternalCodes)
                .orElseGet(Collections::emptySet)
                .forEach(code -> partyRelationUtils.setEmpRelation(employeeId, findOrgId(code), relationCode));
        return employee;
    }

    /**
     * Участник, к которому привязан сотрудник: партнёр, организация или, если не указано, сам сотрудник.
     */
    private String getPartyId(Employee employee) {
        if (employee.getPartyPartnerCode() != null) {
            return partnerUtils.findPartnerIdByCode(employee.getPartyPartnerCode());
        }
        if (employee.getPartyOrgExternalCode() != null) {
            return findOrgId(employee.getPartyOrgExternalCode());
        }
        return employee.getInfo().get(ID);
    }

    private String findOrgId(String externalCode) {
        return organizationUtils.findOrganizationIdByExternalCode(externalCode)
                .orElseThrow(() -> new AutotestException(String.format("Организация '%s' не найдена", externalCode)));
    }
}
