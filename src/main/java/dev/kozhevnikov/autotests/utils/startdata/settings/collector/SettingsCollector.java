package dev.kozhevnikov.autotests.utils.startdata.settings.collector;

import dev.kozhevnikov.autotests.utils.startdata.settings.config.SettingsConfig;

import java.util.List;

public interface SettingsCollector {
    List<SettingsConfig.SettingSpec> collect();

}
