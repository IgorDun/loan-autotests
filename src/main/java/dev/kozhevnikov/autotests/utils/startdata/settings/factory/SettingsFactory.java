package dev.kozhevnikov.autotests.utils.startdata.settings.factory;

import dev.kozhevnikov.autotests.utils.startdata.settings.config.SettingsConfig;

import java.util.List;

public interface SettingsFactory {
    List<SettingsConfig.SettingSpec> create();
}
