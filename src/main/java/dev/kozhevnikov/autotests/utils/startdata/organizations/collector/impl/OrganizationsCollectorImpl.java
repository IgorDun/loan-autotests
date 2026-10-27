package dev.kozhevnikov.autotests.utils.startdata.organizations.collector.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.startdata.StartDataConfig;
import dev.kozhevnikov.autotests.utils.startdata.organizations.collector.OrganizationsCollector;
import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig;
import dev.kozhevnikov.autotests.utils.startdata.utility.YamlLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Читает organizations.yaml, оставляет группы, чьи ключи начинаются с одного из start-data.keys,
 * и проставляет дефолты аккредитаций.
 */
@Component
@RequiredArgsConstructor
public class OrganizationsCollectorImpl implements OrganizationsCollector {

    @Value("classpath:organizations.yaml")
    private Resource resource;
    private final StartDataConfig startDataConfig;

    @Override
    public List<OrganizationsConfig.Organization> collect() {
        Set<String> keys = startDataConfig.getKeys();
        var config = YamlLoader.loadResource(resource, OrganizationsConfig.class);

        return Optional.ofNullable(config.getOrganizations())
                .map(Map::entrySet)
                .orElseGet(Collections::emptySet)
                .parallelStream()
                .filter(entry -> keys.isEmpty() || keys.stream().anyMatch(key -> entry.getKey().startsWith(key)))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .map(org -> org.setAccreditations(OrganizationUnifyHelper.unifyAccreditations(
                        Boolean.valueOf(org.getInfo().get("online")), org.getAccreditations())))
                .collect(Collectors.toList());
    }
}
