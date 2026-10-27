package dev.kozhevnikov.autotests.utils.startdata.settings.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Структура settings.yaml: группы (ключи команд/эпиков) со списком настроек.
 */
@Getter
@Setter
@Accessors(chain = true)
public class SettingsConfig {
    private Map<String, List<SettingSpec>> settings;

    @Data
    @Accessors(chain = true)
    public static class SettingSpec {
        /** settingValue, partyId (необязательно) */
        private Map<String, String> setting;
        /** paramName, paramNameRu, dataType, defaultValue */
        private Map<String, String> param;
        /** subjectType, creationChannel, processCode, linkedProviderId */
        private Map<String, String> subject;
    }
}
