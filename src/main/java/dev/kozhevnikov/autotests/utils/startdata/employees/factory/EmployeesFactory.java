package dev.kozhevnikov.autotests.utils.startdata.employees.factory;

import dev.kozhevnikov.autotests.utils.startdata.employees.config.EmployeesConfig;

import java.util.List;

public interface EmployeesFactory {
    List<EmployeesConfig.Employee> create();
}
