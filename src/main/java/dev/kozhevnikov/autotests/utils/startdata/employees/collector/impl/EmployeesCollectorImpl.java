package dev.kozhevnikov.autotests.utils.startdata.employees.collector.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.startdata.StartDataConfig;
import dev.kozhevnikov.autotests.utils.startdata.employees.collector.EmployeesCollector;
import dev.kozhevnikov.autotests.utils.startdata.employees.config.EmployeesConfig;
import dev.kozhevnikov.autotests.utils.startdata.utility.YamlLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Читает employees.yaml и оставляет группы, чьи ключи начинаются с одного из start-data.keys.
 */
@Component
@RequiredArgsConstructor
public class EmployeesCollectorImpl implements EmployeesCollector {

    @Value("classpath:employees.yaml")
    private Resource resource;
    private final StartDataConfig startDataConfig;

    @Override
    public List<EmployeesConfig.Employee> collect() {
        Set<String> keys = startDataConfig.getKeys();
        var config = YamlLoader.loadResource(resource, EmployeesConfig.class);

        return Optional.ofNullable(config.getEmployees())
                .map(Map::entrySet)
                .orElseGet(Collections::emptySet)
                .parallelStream()
                .filter(entry -> keys.isEmpty() || keys.stream().anyMatch(key -> entry.getKey().startsWith(key)))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
