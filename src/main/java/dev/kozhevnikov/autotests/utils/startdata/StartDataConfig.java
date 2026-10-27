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
 * Переключатели стартовых данных: start-data.generate и start-data.keys (фильтр групп в YAML).
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
        private Set<String> keys;
    }

    public Boolean needCreate() {
        return Optional.ofNullable(this.startData)
                .map(StartData::getGenerate)
                .orElse(false);
    }

    public Set<String> getKeys() {
        return Optional.ofNullable(this.getStartData())
                .map(StartData::getKeys)
                .orElseGet(Collections::emptySet);
    }
}
