package dev.kozhevnikov.autotests.utils.startdata.partners.collector.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.startdata.StartDataConfig;
import dev.kozhevnikov.autotests.utils.startdata.partners.collector.PartnersCollector;
import dev.kozhevnikov.autotests.utils.startdata.partners.config.PartnersConfig;
import dev.kozhevnikov.autotests.utils.startdata.utility.YamlLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Читает partners.yaml и оставляет группы, чьи ключи начинаются с одного из start-data.keys.
 */
@Component
@RequiredArgsConstructor
public class PartnersCollectorImpl implements PartnersCollector {

    @Value("classpath:partners.yaml")
    private Resource resource;
    private final StartDataConfig startDataConfig;

    @Override
    public List<PartnersConfig.Partner> collect() {
        Set<String> keys = startDataConfig.getKeys();
        var config = YamlLoader.loadResource(resource, PartnersConfig.class);

        return Optional.ofNullable(config.getPartners())
                .map(Map::entrySet)
                .orElseGet(Collections::emptySet)
                .parallelStream()
                .filter(entry -> keys.isEmpty() || keys.stream().anyMatch(key -> entry.getKey().startsWith(key)))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
