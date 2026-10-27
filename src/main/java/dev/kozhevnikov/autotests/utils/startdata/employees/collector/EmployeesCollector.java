package dev.kozhevnikov.autotests.utils.startdata.employees.collector;

import dev.kozhevnikov.autotests.utils.startdata.employees.config.EmployeesConfig;

import java.util.List;

public interface EmployeesCollector {
    List<EmployeesConfig.Employee> collect();
}
