package dev.kozhevnikov.autotests.config;

import io.github.bonigarcia.wdm.config.DriverManagerType;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.web.AtWebDriverManager;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Configuration
@Slf4j
@SuppressWarnings("unused")
public class WebDriverConfiguration {

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public DriverManagerType driverManagerType(EnvSettings envSettings) {
        String browserType = envSettings.getBrowserType();
        switch (browserType) {
            case "chrome":
                return DriverManagerType.CHROME;
            case "firefox":
                return DriverManagerType.FIREFOX;
            case "edge":
                return DriverManagerType.EDGE;
            case "iexplorer":
                return DriverManagerType.IEXPLORER;
            default:
                throw new AutotestException(String.format("Браузер '%s' не поддерживается", browserType));
        }
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public Capabilities capabilities(DriverManagerType driverManagerType, EnvSettings envSettings) {
        MutableCapabilities capabilities;
        switch (driverManagerType) {
            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--enable-automation");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-browser-side-navigation");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--use-fake-ui-for-media-stream");
                chromeOptions.addArguments("--use-fake-device-for-media-stream");
                chromeOptions.addArguments("--ignore-certificate-errors");
                // Чистый профиль Chrome после входа показывает окно менеджера паролей
                // («пароль найден в утечке»), которое перехватывает клики по странице
                chromeOptions.addArguments("--disable-features=PasswordLeakDetection,PasswordManagerOnboarding");
                chromeOptions.setExperimentalOption("prefs", Map.of(
                        "credentials_enable_service", false,
                        "profile.password_manager_enabled", false,
                        "profile.password_manager_leak_detection", false));
                // Без окна браузера (CI): -Dheadless=true или переменная окружения HEADLESS=true
                if (Boolean.getBoolean("headless") || Boolean.parseBoolean(System.getenv("HEADLESS"))) {
                    chromeOptions.addArguments("--headless=new", "--window-size=1920,1080");
                }
                capabilities = chromeOptions;
                break;
            case FIREFOX:
                capabilities = new FirefoxOptions();
                break;
            case EDGE:
                capabilities = new EdgeOptions();
                break;
            case IEXPLORER:
                capabilities = new InternetExplorerOptions();
                break;
            default:
                throw new AutotestException("Некорректный тип менеджера вэб-драйвера " + driverManagerType);
        }

        Map<String, Object> checkedOptions = Optional.ofNullable(envSettings.getBrowserOptions()).orElse(Collections.emptyMap());
        checkedOptions.forEach(capabilities::setCapability);
        return capabilities;
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public AtWebDriverManager atWebDriverContainer(EnvSettings envSettings, FileUtils fileUtils) {
        return new AtWebDriverManager(envSettings, fileUtils);
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    @ConditionalOnMissingBean(WebDriver.class)
    public WebDriver webDriver(AtWebDriverManager atWebDriverManager, DriverManagerType driverManagerType, Capabilities capabilities) {
        return atWebDriverManager.createWebDriver(driverManagerType, capabilities);
    }
}
