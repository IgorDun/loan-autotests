package dev.kozhevnikov.autotests.utils.startdata.utility;

import java.util.Map;
import java.util.Optional;

public interface SettingUtils {
    String getSettingSubjects(Map<String, String> settingSubjectFilterDto);

    Optional<String> findParamTypeByParams(Map<String, String>  filter);

    String createParamType(Map<String, String>  enrichParamTypeDto);

    Optional<Map<String, String>> findSettingsByParams(Map<String, String>  settingFilterDto);

    String createSetting(Map<String, String> enrichDtoRequest);

    void updateSetting(String  settingId, Map<String, String > enrichDtoRequest);
}