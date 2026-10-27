package dev.kozhevnikov.autotests.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.springframework.util.FileCopyUtils;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.web.AtWebDriverManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class WebDriverHooks {

    private final AtWebDriverManager atWebDriverManager;
    private final WebDriver webDriver;
    private final EnvSettings envSettings;

    @Before(value = "@webdriver")
    public void startWebDriver() {
        System.setProperty("ATLAS_WEBSITE_URL", "");
        log.info("Браузер успешно запущен:\n" + webDriver);
    }

    @After(value = "@webdriver")
    public void stopWebDriver(Scenario scenario) {
        addAttachments(scenario);
        atWebDriverManager.closeWebDriver(webDriver);
        log.info("Браузер успешно остановлен:\n" + webDriver);
    }

    private void addAttachments(Scenario scenario) {
        attachVideo(scenario);
        if (scenario.isFailed()) {
            try {
                byte[] screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Снимок места падения");
            } catch (Exception e) {
                log.warn("Не удалось сделать снимок при падении теста:\n{}", e.getMessage());
            }

            try {
                Function<LogEntry, String> mapFunction = logEntry -> {
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(logEntry.getTimestamp()), ZoneId.systemDefault());
                    return String.format("%s\t%s\t%s", localDateTime, logEntry.getLevel(), logEntry.getMessage());
                };
                byte[] browserLog = webDriver.manage().logs().get(LogType.BROWSER).getAll().stream()
                        .map(mapFunction)
                        .collect(Collectors.joining("\n"))
                        .getBytes();
                scenario.attach(browserLog, "text/plain", "Лог браузера");
            } catch (Exception e) {
                log.warn("Не удалось получить лог браузера:\n{}", e.getMessage());
            }
        }
    }

    private void attachVideo(Scenario scenario) {
        Path recordingDirectory = Path.of(envSettings.getWebDriverVideoDirectory());
        String videoName = atWebDriverManager.saveVideo(scenario);
        if (Files.exists(recordingDirectory)) {
            try (Stream<Path> videos = Files.walk(recordingDirectory)) {
                videos.filter(path -> path.toString().contains(videoName))
                        .findFirst()
                        .ifPresent(path -> {
                            try {
                                File videoFile = path.toFile();
                                String mediaType = "video/" + envSettings.getWebDriverVideoFormat().toLowerCase();
                                scenario.attach(FileCopyUtils.copyToByteArray(videoFile), mediaType, videoFile.getName());
                            } catch (Exception e) {
                                log.warn("Не удалось прикрепить видеозапись прохождения теста:\n{}", e.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.warn("Не удалось получить список видеозаписей прохождения тестов:\n{}", e.getMessage());
            }
        }
    }
}
