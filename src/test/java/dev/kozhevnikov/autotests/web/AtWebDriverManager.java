package dev.kozhevnikov.autotests.web;

import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.config.WebDriverManagerException;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.RecordingFileFactory;
import org.testcontainers.containers.SocatContainer;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;
import org.testcontainers.lifecycle.TestDescription;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.common.FileUtils;

import jakarta.annotation.PreDestroy;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@Lazy
@Scope("cucumber-glue")
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Slf4j
@SuppressWarnings("unused")
public class AtWebDriverManager {

    private static final String TESTCONTAINERS_DRIVER_TYPE = "testcontainers";
    private static final String LOCAL_DRIVER_TYPE = "local";

    private Network network;
    private SocatContainer socatContainer;
    private BrowserWebDriverContainer<?> webDriverContainer;
    private final EnvSettings envSettings;
    private final FileUtils fileUtils;

    public WebDriver createWebDriver(DriverManagerType driverManagerType, Capabilities capabilities) {
        WebDriver webDriver;
        String webDriverType = envSettings.getWebDriverType();
        if (TESTCONTAINERS_DRIVER_TYPE.equalsIgnoreCase(webDriverType)) {
            runContainers(capabilities);
            webDriver = WebDriverManager.getInstance(driverManagerType)
                    .capabilities(capabilities)
                    .remoteAddress(webDriverContainer.getSeleniumAddress())
                    .create();
            ((RemoteWebDriver) webDriver).setFileDetector(new LocalFileDetector());
            webDriver.manage().window().setSize(new Dimension(1360, 768));
        } else if (LOCAL_DRIVER_TYPE.equalsIgnoreCase(webDriverType)) {
            disableSsl();
            try {
                webDriver = WebDriverManager.getInstance(driverManagerType)
                        .capabilities(capabilities)
                        .create();
            } catch (WebDriverManagerException ex) {
                // Для закрытой сети задайте зеркало: -Dwdm.chromeDriverMirrorUrl=... (см. документацию WebDriverManager)
                throw new AutotestException(String.format("Не удалось загрузить веб-драйвер '%s': %s",
                        driverManagerType, ex.getMessage()));
            }
        } else {
            throw new AutotestException(String.format("Тип веб-драйвера '%s' не поддерживается", webDriverType));
        }

        return webDriver;
    }

    public void closeWebDriver(WebDriver webDriver) {
        RemoteWebDriver remoteWebDriver = (RemoteWebDriver) webDriver;

        try {
            remoteWebDriver.manage().deleteAllCookies();
        } catch (Exception e) {
            log.warn("Не удалось удалить cookies: " + e.getMessage());
        }

        try {
            String js = fileUtils.readFile(fileUtils.getResourceFile("clearLocalStorageScript"));
            remoteWebDriver.executeScript(js);
        } catch (Exception e) {
            log.warn("не удалось очистить локальное хранилище: " + e.getMessage());
        }

        try {
            String js = fileUtils.readFile(fileUtils.getResourceFile("clearSessionStorageScript"));
            remoteWebDriver.executeScript(js);
        } catch (Exception e) {
            log.warn("Не удалось очистить сессионное хранилище: " + e.getMessage());
        }

        try {
            webDriver.quit();
        } catch (WebDriverException e) {
            log.error(e.getMessage());
        }
    }

    public String saveVideo(Scenario scenario) {
        String videoName;
        if (TESTCONTAINERS_DRIVER_TYPE.equalsIgnoreCase(envSettings.getWebDriverType())) {
            String resultMarker = scenario.isFailed() ? "FAILED" : "PASSED";
            String normalizedScenarioName = scenario.getName().replaceAll("[.,:;?!+#№%&*|\\s/\\\\]", "_");
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            videoName = String.format("%s-%s-%s", normalizedScenarioName, resultMarker, date);

            RecordingFileFactory recordingFileFactory = (File vncRecordingDirectory, String prefix, boolean succeeded) ->
                    new File(vncRecordingDirectory, String.format("%s.%s", videoName, envSettings.getWebDriverVideoFormat().toLowerCase()));

            webDriverContainer.withRecordingFileFactory(recordingFileFactory);

            TestDescription testDescription = new TestDescription() {
                @Override
                public String getTestId() {
                    return scenario.getId();
                }

                @Override
                public String getFilesystemFriendlyName() {
                    return normalizedScenarioName;
                }
            };

            Optional<Throwable> optionalThrowable = scenario.isFailed() ? Optional.of(new AutotestException(scenario.getName() + " failed")) : Optional.empty();
            webDriverContainer.afterTest(testDescription, optionalThrowable);

            return videoName;
        } else {
            log.warn("Невозможно сохранить видеозапись теста. Запись видео поддерживается только для удаленного вэб-драйвера");
            return null;
        }
    }

    @SuppressWarnings("resource")
    private void runContainers(Capabilities capabilities) {
        network = Network.newNetwork();

        socatContainer = new SocatContainer()
                .withTarget(2000, envSettings.getBrowserType(), 4444)
                .withNetwork(network);
        socatContainer.start();

        BrowserWebDriverContainer.VncRecordingMode recordingMode;
        String webDriverVideoRecording = envSettings.getWebDriverVideoRecording();
        if ("ALL".equalsIgnoreCase(webDriverVideoRecording)) {
            recordingMode = BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;
        } else if ("ERRORS".equalsIgnoreCase(webDriverVideoRecording)) {
            recordingMode = BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;
        } else if ("NONE".equalsIgnoreCase(webDriverVideoRecording)) {
            recordingMode = BrowserWebDriverContainer.VncRecordingMode.SKIP;
        } else {
            throw new AutotestException(String.format("Режим видеозаписи '%s' не поддерживается (регистр не важен)", webDriverVideoRecording));
        }

        Path videoDirectory = Path.of(envSettings.getWebDriverVideoDirectory());
        if (!recordingMode.equals(BrowserWebDriverContainer.VncRecordingMode.SKIP) && Files.notExists(videoDirectory)) {
            try {
                Files.createDirectories(videoDirectory);
            } catch (IOException e) {
                throw new AutotestException(e);
            }
        }

        VncRecordingContainer.VncRecordingFormat vncRecordingFormat = VncRecordingContainer.VncRecordingFormat.valueOf(envSettings.getWebDriverVideoFormat().toUpperCase());

        webDriverContainer = new BrowserWebDriverContainer<>()
                .withEnv("SE_NODE_GRID_URL", String.format("http://%s:%d", socatContainer.getHost(), socatContainer.getMappedPort(2000)))
                .withNetworkAliases(envSettings.getBrowserType())
                .withNetwork(network)
                .withCapabilities(capabilities)
                .withRecordingMode(recordingMode, videoDirectory.toFile(), vncRecordingFormat)
                .withStartupCheckStrategy(new IsRunningStartupCheckStrategy());
        webDriverContainer.start();
    }

    private void disableSsl() {
        try {
            var sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (GeneralSecurityException e) {
            throw new AutotestException("Не удалось отключить SSL", e);
        }
    }

    @PreDestroy
    private void tearDown() {
        Consumer<? super GenericContainer<?>> containerConsumer = container -> {
            container.stop();
            container.close();
        };
        Optional.ofNullable(webDriverContainer).ifPresent(containerConsumer);
        Optional.ofNullable(socatContainer).ifPresent(containerConsumer);
        Optional.ofNullable(network).ifPresent(Network::close);
    }

}
