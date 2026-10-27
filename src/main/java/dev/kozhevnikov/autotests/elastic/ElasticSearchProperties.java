package dev.kozhevnikov.autotests.elastic;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

/**
 * Настройки Elasticsearch из elastic-search.properties.
 * indexes — индексы по стендам, indexesKey — ключ текущего стенда.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
@PropertySource(value = "classpath:elastic-search.properties", encoding = "UTF-8")
public class ElasticSearchProperties {
    private String url;
    private String username;
    private String password;
    private String indexesKey;
    private Map<String, String> indexes;

    public String currentIndexes() {
        String value = indexes == null ? null : indexes.get(indexesKey);
        if (value == null) {
            throw new IllegalStateException("Не заданы индексы Elasticsearch для стенда '" + indexesKey + "'");
        }
        return value;
    }
}
