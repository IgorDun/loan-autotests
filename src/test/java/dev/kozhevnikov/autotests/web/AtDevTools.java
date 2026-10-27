package dev.kozhevnikov.autotests.web;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v155.network.Network;
import org.openqa.selenium.devtools.v155.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v155.network.model.ResourceType;
import org.openqa.selenium.devtools.v155.network.model.ResponseReceived;
import org.openqa.selenium.remote.Augmenter;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.exception.AutotestException;

import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope("cucumber-glue")
@EqualsAndHashCode
@SuppressWarnings("unused")
public class AtDevTools {
    @Getter
    private RequestWillBeSent lastRequest;
    @Getter
    private ResponseReceived lastResponse;
    private final DevTools devTools;
    /** Фильтр XHR-запросов, которые сохраняются для проверок: -Ddevtools.urlFilter=api.example.com */
    private static final String URL_FOR_SEARCH = System.getProperty("devtools.urlFilter", "/api/");
    private static final String URL_FOR_EXCLUSION = "yandex";
    @Getter
    private Map<LocalDateTime, ResponseReceived> allResponses = new LinkedHashMap<>();

    public AtDevTools(WebDriver webDriver) {
        WebDriver driver = new Augmenter().augment(webDriver);
        devTools = ((HasDevTools) driver).maybeGetDevTools()
                .orElseThrow(() -> new AutotestException("Не удалось получить dev tools для вэб-драйвера " + driver));
        devTools.createSession();

        devTools.send(Network.enable(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.requestWillBeSent(), request -> {
            String url = request.getRequest().getUrl();
            if (request.getType().equals(Optional.of(ResourceType.XHR))
                    && url.contains(URL_FOR_SEARCH)
                    && !url.contains(URL_FOR_EXCLUSION)) {
                this.lastRequest = request;
            }
        });

        devTools.addListener(Network.responseReceived(), response -> {
            String url = response.getResponse().getUrl();
            if (response.getType().equals(ResourceType.XHR)
                    && url.contains(URL_FOR_SEARCH)
                    && !url.contains(URL_FOR_EXCLUSION)) {
                this.lastResponse = response;
                allResponses.put(LocalDateTime.now(), response);
                reverseSortByDateTime();
            }
        });
    }

    public <T> T send(Command<T> command) {
        return devTools.send(command);
    }

    public <T> void addListener(Event<T> event, Consumer<T> handler) {
        devTools.addListener(event, handler);
    }

    @PreDestroy
    private void tearDown() {
        devTools.close();
    }
    public void reverseSortByDateTime() {
        allResponses = allResponses.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }
}