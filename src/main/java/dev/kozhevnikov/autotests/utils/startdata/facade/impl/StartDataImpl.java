package dev.kozhevnikov.autotests.utils.startdata.facade.impl;

import dev.kozhevnikov.autotests.utils.startdata.employees.config.EmployeesConfig.Employee;
import dev.kozhevnikov.autotests.utils.startdata.employees.factory.EmployeesFactory;
import dev.kozhevnikov.autotests.utils.startdata.facade.StartData;
import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Organization;
import dev.kozhevnikov.autotests.utils.startdata.organizations.factory.OrganizationsFactory;
import dev.kozhevnikov.autotests.utils.startdata.partners.config.PartnersConfig.Partner;
import dev.kozhevnikov.autotests.utils.startdata.partners.factory.PartnersFactory;
import dev.kozhevnikov.autotests.utils.startdata.utility.StartDataContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class StartDataImpl implements StartData {
    private final PartnersFactory partnersFactory;
    private final OrganizationsFactory organizationsFactory;
    private final EmployeesFactory employeesFactory;

    @Override
    public List<Partner> getPartners() {
        return getOrCreate("partners", Partner.class, partnersFactory::create);
    }

    @Override
    public List<Organization> getOrganizations() {
        return getOrCreate("organizations", Organization.class, organizationsFactory::create);
    }

    @Override
    public List<Employee> getEmployees() {
        return getOrCreate("employees", Employee.class, employeesFactory::create);
    }

    private <T> List<T> getOrCreate(String key, Class<T> type, Supplier<List<T>> factory) {
        return Optional.ofNullable(StartDataContextUtil.getList(key, type))
                .orElseGet(() -> {
                    List<T> created = factory.get();
                    StartDataContextUtil.put(key, created);
                    return created;
                });
    }
}
