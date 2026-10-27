package dev.kozhevnikov.autotests.utils.startdata.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.startdata.facade.StartData;
import dev.kozhevnikov.autotests.utils.startdata.settings.config.SettingsConfig.SettingSpec;
import dev.kozhevnikov.autotests.utils.startdata.settings.factory.SettingsFactory;
import dev.kozhevnikov.autotests.utils.startdata.utility.StartDataContextUtil;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StartDataImpl implements StartData {
    private final SettingsFactory settingsFactory;

    @Override
    public List<SettingSpec> getSettings() {
        return Optional.ofNullable(StartDataContextUtil.getList("settings", SettingSpec.class))
                .orElseGet(() -> {
                    List<SettingSpec> created = settingsFactory.create();
                    StartDataContextUtil.put("settings", created);
                    return created;
                });
    }
}
