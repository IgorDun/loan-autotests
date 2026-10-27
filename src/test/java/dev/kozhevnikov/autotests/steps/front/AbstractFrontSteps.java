package dev.kozhevnikov.autotests.steps.front;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import io.qameta.atlas.core.Atlas;
import io.qameta.atlas.webdriver.ElementsCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.awaitility.core.ConditionFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.config.CucumberConfiguration;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.config.ServiceConfiguration;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.common.WaitUtils;
import dev.kozhevnikov.autotests.web.AtDevTools;
import dev.kozhevnikov.autotests.web.elements.DropBoxElement;
import dev.kozhevnikov.autotests.web.elements.InputTextElement;
import dev.kozhevnikov.autotests.web.elements.InputTextSuggestionElement;
import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.elements.SelectableElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import dev.kozhevnikov.autotests.web.sites.Sites;
import dev.kozhevnikov.autotests.web.sites.front.AbstractSite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Lazy
@Scope("cucumber-glue")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AbstractFrontSteps extends CucumberConfiguration {
    private final FileUtils fileUtils;
    private final WaitUtils waitUtils;
    private final Enricher enricher;
    private final EnvSettings envSettings;
    private final ServiceConfiguration serviceConfiguration;
    private final Atlas atlas;
    private final WebDriver webDriver;
    private final AtDevTools atDevTools;

    public <T extends AbstractSite> T onSite(String siteName) {
        return getWebSite(Sites.filterSite(siteName));
    }

    public Atlas getAtlas() {
        return atlas;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public AtDevTools getAtDevTools() {
        return atDevTools;
    }

    public boolean isPresent(BaseElement element) {
        return isPresent(element, envSettings.getFrontFastTimeoutInSeconds());
    }

    public boolean isPresent(BaseElement element, long timeout) {
        ConditionFactory condition = waitUtils.getDefaultSettings()
                .timeout(timeout, TimeUnit.SECONDS)
                .pollInterval(50, TimeUnit.MILLISECONDS)
                .pollDelay(299, TimeUnit.MILLISECONDS);
        Callable<Boolean> booleanPredicate = () -> {
            try {
                return element.isEnabled();
            } catch (NoSuchElementException e) {
                return false;
            }
        };
        return waitUtils.waitUntil(condition, booleanPredicate);
    }

    public BaseElement hover(BaseElement element) {
        String js = fileUtils.readFile(fileUtils.getResourceFile("hoverScript"));
        ((JavascriptExecutor) element.getWrappedDriver()).executeScript(js, element);
        return element;
    }

    public BaseElement scrollTo(BaseElement element, boolean top) {
        String js = fileUtils.readFile(fileUtils.getResourceFile("scrollIntoViewScript"));
        ((JavascriptExecutor) element.getWrappedDriver()).executeScript(js, element, top);
        return element;
    }

    public void scrollHorizontal(BaseElement scrollableElement, int width) {
        String js = fileUtils.readFile(fileUtils.getResourceFile("scrollHorizontalScript"));
        ((JavascriptExecutor) scrollableElement.getWrappedDriver()).executeScript(js, scrollableElement, width);
    }

    public void replaceInputValue(InputTextElement inputTextElement, String text) {
        String js = fileUtils.readFile(fileUtils.getResourceFile("selectScript"));
        ((JavascriptExecutor) inputTextElement.getWrappedDriver()).executeScript(js, inputTextElement);
        inputTextElement.sendKeys(text);
    }

    public void waitUntilNotPresent(BaseElement element, long seconds) {
        waitUtils.waitUntil(waitUtils.getDefaultSettings().timeout(seconds, TimeUnit.SECONDS),
                () -> !isPresent(element));
    }

    public void setText(InputTextElement inputTextElement, String text) {
        if (inputTextElement instanceof InputTextSuggestionElement) {
            InputTextSuggestionElement inputTextSuggestionElement = (InputTextSuggestionElement) inputTextElement;
            replaceInputValue(inputTextSuggestionElement, text);
            ConditionFactory conditionFactory = waitUtils.getDefaultSettings()
                    .timeout(envSettings.getFrontFastTimeoutInSeconds(), TimeUnit.SECONDS)
                    .pollDelay(1, TimeUnit.SECONDS)
                    .pollInterval(50, TimeUnit.MILLISECONDS);
            boolean isEmptyList = waitUtils.waitUntil(conditionFactory, () -> !inputTextSuggestionElement.suggestionsList().isEmpty());
            if (isEmptyList) {
                Optional<BaseElement> matchedElement = Optional.empty();
                try {
                    matchedElement = inputTextSuggestionElement.suggestionsList()
                            .stream()
                            .filter(element -> element.getText().equals(text))
                            .findFirst();
                } catch (Exception e) {
                    log.error("Значение в выпадающем списке не соответствует введённому - {}", text);
                }

                matchedElement.ifPresentOrElse(
                        elem -> {
                            elem.click();
                            log.info("Выбрал значение из выпадающего списка - {}", text);
                        },
                        () -> log.info("Соответствующее значение в выпадающем списке не найдено - {}", text)
                );
            } else {
                log.info("Выпадающий список пуст");
            }
        } else {
            inputTextElement.setText(text);
        }
    }

    public void jsClick(BaseElement element) {
        String js = fileUtils.readFile(fileUtils.getResourceFile("clickScript"));
        ((JavascriptExecutor) element.getWrappedDriver()).executeScript(js, element);
    }

    public <T extends AbstractPage> void fillForm(String siteName, Class<T> pageType, Map<String, String> params) {
        params.forEach((key, value) -> {
            AbstractSite site = onSite(siteName);
            Object field = site.onPage(pageType).getElementByAnnotationName(key);
            String enrichedValue = enricher.enrich(value);
            if (field instanceof InputTextElement) {
                setText(((InputTextElement) field), enrichedValue);
            } else if (field instanceof SelectableElement) {
                ((SelectableElement) field).select(Boolean.parseBoolean(enrichedValue));
            } else {
                ((DropBoxElement) field).selectValue(enrichedValue, text -> text.startsWith(enrichedValue));
            }
            LoggerFactory.getLogger(site.getClass()).info("Заполнил поле - {}, значением - {}", key, enrichedValue);
        });
    }

    public <T extends AbstractPage> void fillFormIndex(String siteName, Class<T> pageType, List<Map<String, String>> listParams) {
        for (int line = 0; line < listParams.size(); line++) {
            String lineNumber = String.valueOf(line);
            listParams.get(line).forEach((key, value) -> {
                if (value != null) {
                    AbstractSite site = onSite(siteName);
                    Object field = site.onPage(pageType).getElementByAnnotationNameIndex(key, lineNumber);
                    String enrichedValue = enricher.enrich(value);
                    if (field instanceof InputTextElement) {
                        setText(((InputTextElement) field), enrichedValue);
                    } else if (field instanceof SelectableElement) {
                        ((SelectableElement) field).select(Boolean.parseBoolean(enrichedValue));
                    } else {
                        ((DropBoxElement) field).selectValue(enrichedValue, text -> text.startsWith(enrichedValue));
                    }
                    LoggerFactory.getLogger(site.getClass()).info("Заполнил поле - {}, значением - {}", key, enrichedValue);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractPage> void fillCollectionByIndex(String siteName, Class<T> pageType, String key, String value, int indexElement) {
        AbstractSite site = onSite(siteName);
        Object field = site.onPage(pageType).getElementByAnnotationName(key);
        String enrichedValue = enricher.enrich(value);
        setText(((ElementsCollection<InputTextElement>) field).get(indexElement), enrichedValue);
        LoggerFactory.getLogger(site.getClass()).info("Заполнил поле - {}, значением - {}", key, enrichedValue);
    }

    public String getTextFromQr(BaseElement qr) throws NotFoundException, IOException {
        scrollTo(qr, true);

        File qrImage = qr.getScreenshotAs(OutputType.FILE);
        BufferedImage bufferedImage = ImageIO.read(qrImage);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        String qrText = new MultiFormatReader().decode(bitmap).getText();

        log.info("Получен текст из QR-кода: {}", qrText);

        return qrText;
    }

    public void openUrlOnNewWindow(String url) {
        AutoTestContext.put("url", url);
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript(enricher.enrich(fileUtils.readFile(fileUtils.getResourceFile("newWindowScript"))));
        switchWindowTabOnNumber(2);
        log.info("Открыта ссылка '{}' в новой вкладке", url);
    }

    public void closeSecondWindow() {
        switchWindowTabOnNumber(2).close();
        switchWindowTabOnNumber(1);
        log.info("Закрыта дополнительная вкладка");
    }

    public WebDriver switchWindowTabOnNumber(int numberOfWindow) {
        return webDriver.switchTo().window(webDriver.getWindowHandles().toArray()[numberOfWindow - 1].toString());
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractSite> T getWebSite(Class<? extends AbstractSite> siteClass) {
        String siteName = Sites.filterSite(siteClass);
        String baseUrl = serviceConfiguration.getService(siteName).getUrl();
        System.getProperties().setProperty("ATLAS_WEBSITE_URL", baseUrl);
        return (T) atlas.create(webDriver, siteClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractPage> void checkTableRow(String siteName, Class<T> pageType, Integer row, Map<String, String> params) {
        AbstractSite site = onSite(siteName);
        params.forEach((key, value) -> {
            List<BaseElement> field = (List<BaseElement>) site.onPage(pageType).getElementByAnnotationName(key);
            String enrichedValue = enricher.enrich(value);
            Assertions.assertThat(field.get(row).getText())
                    .withFailMessage("Неверное значение в строке %d", row)
                    .isEqualTo(enrichedValue);
            LoggerFactory.getLogger(site.getClass()).info("Заполнил поле - {}, значением - {}", key, enrichedValue);
        });
    }

    public void pressKey(Keys key) {
        new Actions(webDriver)
                .sendKeys(key)
                .perform();

        log.info("Нажали на клавишу '{}'", key.toString());
    }
}