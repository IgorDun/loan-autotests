package dev.kozhevnikov.autotests.utils.startdata.settings.factory.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.startdata.settings.collector.SettingsCollector;
import dev.kozhevnikov.autotests.utils.startdata.settings.config.SettingsConfig.SettingSpec;
import dev.kozhevnikov.autotests.utils.startdata.settings.factory.SettingsFactory;
import dev.kozhevnikov.autotests.utils.startdata.utility.SettingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettingsFactoryImpl implements SettingsFactory {
    private final SettingsCollector settingsCollector;
    private final SettingUtils settingUtils;

    @Override
    public List<SettingSpec> create() {
        return settingsCollector.collect()
                .parallelStream()
                .map(this::setSettingSubjectId)
                .map(this::createOrGetParamType)
                .map(this::createOrGetSetting)
                .collect(Collectors.toList());
    }

    private SettingSpec setSettingSubjectId(SettingSpec spec) {
        Map<String, String> settingSubjectFilterDto = new HashMap<>();

        Optional.ofNullable(spec.getSubject().get("subjectType")).ifPresent(subjectType -> settingSubjectFilterDto.put("subjectType", subjectType));
        Optional.ofNullable(spec.getSetting().get("partyId")).ifPresent(partyId -> settingSubjectFilterDto.put("partyId", partyId));
        Optional.ofNullable(spec.getSubject().get("processCode")).ifPresent(processCode -> settingSubjectFilterDto.put("processCode", processCode));
        Optional.ofNullable(spec.getSubject().get("linkedProviderId")).ifPresent(linkedProviderId -> settingSubjectFilterDto.put("linkedProviderId", linkedProviderId));

        String settingSubjectId = this.settingUtils.getSettingSubjects(settingSubjectFilterDto);
        spec.getSubject().put("settingSubjectId", settingSubjectId);
        return spec;
    }

    private SettingSpec createOrGetParamType(SettingSpec spec) {
        Map<String, String> filter = new HashMap<>();

        Optional.ofNullable(spec.getParam().get("paramName")).ifPresent(paramNames -> filter.put("paramNames", paramNames));
        Optional.ofNullable(spec.getSubject().get("subjectType")).ifPresent(subjectType -> filter.put("subjectType", subjectType));
        Optional.ofNullable(spec.getParam().get("dataType")).ifPresent(dataType -> filter.put("dataType", dataType));

        String paramTypeId = settingUtils.findParamTypeByParams(filter)
                .orElseGet(() -> settingUtils.createParamType(enrichParamTypeDto(spec)));

        spec.getParam().put("paramTypeId", paramTypeId);
        return spec;
    }

    private Map<String, String> enrichParamTypeDto(SettingSpec spec) {
        Map<String, String> paramType = new HashMap<>();
        Optional.ofNullable(spec.getParam().get("dataType")).ifPresent(dataType -> paramType.put("dataType", dataType));
        Optional.ofNullable(spec.getParam().get("paramName")).ifPresent(paramName -> paramType.put("paramName", paramName));
        Optional.ofNullable(spec.getSubject().get("subjectType")).ifPresent(subjectType -> paramType.put("subjectType", subjectType));
        Optional.ofNullable(spec.getParam().get("paramDescription")).ifPresent(paramDescription -> paramType.put("paramDescription", paramDescription));
        Optional.ofNullable(spec.getParam().get("defaultValue")).ifPresent(defaultValue -> paramType.put("defaultValue", defaultValue));
        Optional.ofNullable(spec.getParam().get("paramNameRu")).ifPresent(paramNameRu -> paramType.put("paramNameRu", paramNameRu));
        return paramType;
    }

    private SettingSpec createOrGetSetting(SettingSpec spec) {
        Map<String, String> settingFilterDto = new HashMap<>();

        Optional.ofNullable(spec.getParam().get("paramName")).ifPresent(paramNames -> settingFilterDto.put("paramNames", paramNames));
        Optional.ofNullable(spec.getSetting().get("partyId")).ifPresent(partyId -> settingFilterDto.put("partyId", partyId));
        Optional.ofNullable(spec.getSubject().get("settingSubjectId")).ifPresent(settingSubjectId -> settingFilterDto.put("settingSubjectId", settingSubjectId));

        String settingId = settingUtils.findSettingsByParams(settingFilterDto)
                .map(response -> {
                    if (!checkValue(response, spec)) {
                        settingUtils.updateSetting(response.get("settingId"), enrichDtoRequest(spec));
                    }
                    return response.get("settingId");
                })
                .orElseGet(() -> settingUtils.createSetting(enrichDtoRequest(spec)));

        spec.getSetting().put("settingId", settingId);
        return spec;
    }

    private Map<String, String> enrichDtoRequest(SettingSpec spec) {

        return Map.of("paramTypeId", spec.getParam().get("paramTypeId"),
                "settingSubjectId", spec.getSubject().get("settingSubjectId"),
                "paramValue", spec.getSetting().get("settingValue"));
    }

    private boolean checkValue(Map<String, String> response, SettingSpec spec) {
        return response.get("paramValue").equals(spec.getSetting().get("settingValue"));
    }
}