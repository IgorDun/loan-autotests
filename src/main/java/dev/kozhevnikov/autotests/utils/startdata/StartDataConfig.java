package dev.kozhevnikov.autotests.utils.startdata;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Переключатели стартовых данных: startData.generate (организации и сотрудники),
 * startData.generatePartners (справочник партнёров, обычно уже есть на стенде)
 * и startData.keys (фильтр групп в YAML по префиксу ключа команды).
 */
@Getter
@Setter
@Configuration
@Accessors(chain = true)
@ConfigurationProperties
public class StartDataConfig {
    private StartData startData;

    @Data
    @Accessors(chain = true)
    public static class StartData {
        private Boolean generate;
        private Boolean generatePartners;
        private Set<String> keys;
    }

    public Boolean needCreate() {
        return Optional.ofNullable(this.startData)
                .map(StartData::getGenerate)
                .orElse(false);
    }

    public Boolean needCreatePartners() {
        return Optional.ofNullable(this.startData)
                .map(StartData::getGeneratePartners)
                .orElse(false);
    }

    public Set<String> getKeys() {
        return Optional.ofNullable(this.getStartData())
                .map(StartData::getKeys)
                .orElseGet(Collections::emptySet);
    }
}
