package dev.kozhevnikov.autotests.utils.startdata.settings.collector.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.startdata.StartDataConfig;
import dev.kozhevnikov.autotests.utils.startdata.settings.collector.SettingsCollector;
import dev.kozhevnikov.autotests.utils.startdata.settings.config.SettingsConfig;
import dev.kozhevnikov.autotests.utils.startdata.utility.YamlLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettingsCollectorImpl implements SettingsCollector {

    @Value("classpath:settings.yaml")
    private Resource settingsResource;
    private final StartDataConfig startDataConfig;

    @Override
    public List<SettingsConfig.SettingSpec> collect() {
        Set<String> keys = startDataConfig.getKeys();
        var config = YamlLoader.loadResource(settingsResource, SettingsConfig.class);
        return Optional.ofNullable(config.getSettings())
                .map(Map::entrySet)
                .orElseGet(Collections::emptySet)
                .parallelStream()
                .filter(entry -> keys.isEmpty() || keys.stream().anyMatch(key -> entry.getKey().startsWith(key)))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }
}
