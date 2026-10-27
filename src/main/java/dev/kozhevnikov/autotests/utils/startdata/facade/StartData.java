package dev.kozhevnikov.autotests.utils.startdata.facade;

import dev.kozhevnikov.autotests.utils.startdata.settings.config.SettingsConfig.SettingSpec;

import java.util.List;

/**
 * Фасад стартовых данных: возвращает закешированные данные или создаёт их через фабрики.
 * Новый домен добавляется методом здесь и парой collector/factory в своём пакете.
 */
public interface StartData {
    List<SettingSpec> getSettings();
}
