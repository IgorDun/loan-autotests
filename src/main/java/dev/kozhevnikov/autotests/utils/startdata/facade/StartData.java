package dev.kozhevnikov.autotests.utils.startdata.facade;

import dev.kozhevnikov.autotests.utils.startdata.employees.config.EmployeesConfig.Employee;
import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Organization;
import dev.kozhevnikov.autotests.utils.startdata.partners.config.PartnersConfig.Partner;

import java.util.List;

/**
 * Фасад стартовых данных: возвращает закешированные данные или создаёт их через фабрики.
 * Новый домен добавляется методом здесь и тройкой config/collector/factory в своём пакете.
 * Порядок вызова важен: организации ссылаются на партнёров, сотрудники — на организации и партнёров.
 */
public interface StartData {
    List<Partner> getPartners();

    List<Organization> getOrganizations();

    List<Employee> getEmployees();
}
