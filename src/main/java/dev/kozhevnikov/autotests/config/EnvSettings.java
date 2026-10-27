package dev.kozhevnikov.autotests.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
@Getter
@EqualsAndHashCode
@ToString
public class EnvSettings {

    private final String inventoryName;
    private final long apiTimeoutInSeconds;
    private final long apiIntervalInSeconds;
    private final long frontTimeoutInSeconds;
    private final long frontFastTimeoutInSeconds;
    private final long frontLongTimeoutInSeconds;
    private final long logsLongTimeoutInSeconds;
    private final long devToolsFastIntervalInMilliseconds;
    private final long devToolsIntervalInMilliseconds;
    private final long devToolsTimeoutInSeconds;
    private final long devToolsLongTimeoutInSeconds;
    private final int windowResizeAttempts;
    private final String startToken;
    private final String browserType;
    private final Map<String, Object> browserOptions;
    private final String webDriverType;
    private final String webDriverVideoRecording;
    private final String webDriverVideoFormat;
    private final String webDriverVideoDirectory;

    @SuppressWarnings("unused")
    private EnvSettings(@Value("${env.inventoryName}") String inventoryName,
                        @Value("${env.apiTimeoutInSeconds}") long apiTimeoutInSeconds,
                        @Value("${env.apiIntervalInSeconds}") long apiIntervalInSeconds,
                        @Value("${env.frontTimeoutInSeconds}") long frontTimeoutInSeconds,
                        @Value("${env.frontFastTimeoutInSeconds}") long frontFastTimeoutInSeconds,
                        @Value("${env.frontLongTimeoutInSeconds}") long frontLongTimeoutInSeconds,
                        @Value("${env.logsLongTimeoutInSeconds}") long logsLongTimeoutInSeconds,
                        @Value("${env.devToolsFastIntervalInMilliseconds}") long devToolsFastIntervalInMilliseconds,
                        @Value("${env.devToolsIntervalInMilliseconds}") long devToolsIntervalInMilliseconds,
                        @Value("${env.devToolsTimeoutInSeconds}") long devToolsTimeoutInSeconds,
                        @Value("${env.devToolsLongTimeoutInSeconds}") long devToolsLongTimeoutInSeconds,
                        @Value("${env.windowResizeAttempts}") int windowResizeAttempts,
                        @Value("${env.startToken:}") String startToken,
                        @Value("${env.browserType}") String browserType,
                        @Value("${env.browserOptions}") Map<String, Object> browserOptions,
                        @Value("${env.webDriverType}") String webDriverType,
                        @Value("${env.webDriverVideoRecording}") String webDriverVideoRecording,
                        @Value("${env.webDriverVideoFormat}") String webDriverVideoFormat,
                        @Value("${env.webDriverVideoDirectory}") String webDriverVideoDirectory) {
        this.inventoryName = inventoryName;
        this.apiTimeoutInSeconds = apiTimeoutInSeconds;
        this.apiIntervalInSeconds = apiIntervalInSeconds;
        this.frontTimeoutInSeconds = frontTimeoutInSeconds;
        this.frontFastTimeoutInSeconds = frontFastTimeoutInSeconds;
        this.frontLongTimeoutInSeconds = frontLongTimeoutInSeconds;
        this.logsLongTimeoutInSeconds = logsLongTimeoutInSeconds;
        this.devToolsFastIntervalInMilliseconds = devToolsFastIntervalInMilliseconds;
        this.devToolsIntervalInMilliseconds = devToolsIntervalInMilliseconds;
        this.devToolsTimeoutInSeconds = devToolsTimeoutInSeconds;
        this.devToolsLongTimeoutInSeconds = devToolsLongTimeoutInSeconds;
        this.windowResizeAttempts = windowResizeAttempts;
        this.startToken = startToken;
        this.browserType = browserType;
        this.browserOptions = browserOptions;
        this.webDriverType = webDriverType;
        this.webDriverVideoRecording = webDriverVideoRecording;
        this.webDriverVideoFormat = webDriverVideoFormat;
        this.webDriverVideoDirectory = webDriverVideoDirectory;
    }
}