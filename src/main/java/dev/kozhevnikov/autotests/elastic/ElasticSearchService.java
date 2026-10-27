package dev.kozhevnikov.autotests.elastic;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Поиск логов в Elasticsearch через REST API _search по индексам текущего стенда.
 */
@Lazy
@Component
@RequiredArgsConstructor
public class ElasticSearchService {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final ElasticSearchProperties properties;
    private final WebClient httpWebClient;

    public SearchRequestBuilder searchRequest() {
        return new SearchRequestBuilder();
    }

    public SearchResponse search(SearchRequest request) {
        return httpWebClient.post()
                .uri(properties.getUrl() + "/" + properties.currentIndexes() + "/_search")
                .header(HttpHeaders.AUTHORIZATION, basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .block(TIMEOUT);
    }

    private String basicAuth() {
        String credentials = properties.getUsername() + ":" + properties.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
