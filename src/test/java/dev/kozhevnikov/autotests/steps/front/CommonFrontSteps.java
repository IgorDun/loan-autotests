package dev.kozhevnikov.autotests.steps.front;

import com.google.zxing.NotFoundException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.core.ConditionFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.config.ServiceConfiguration;
import dev.kozhevnikov.autotests.exception.AutotestException;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;
import dev.kozhevnikov.autotests.utils.common.WaitUtils;
import dev.kozhevnikov.autotests.web.elements.InputTextElement;
import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import dev.kozhevnikov.autotests.web.sites.Sites;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.not;
import static dev.kozhevnikov.autotests.web.matchers.WebElementMatchers.displayed;
import static dev.kozhevnikov.autotests.web.matchers.WebElementMatchers.enabled;

@Slf4j
@RequiredArgsConstructor
public class CommonFrontSteps {
    private final AbstractFrontSteps abstractFrontSteps;
    private final Enricher enricher;
    private final WaitUtils waitUtils;
    private final EnvSettings envSettings;
    private final ServiceConfiguration serviceConfiguration;
    private final FileUtils fileUtils;

    /**
     * --------
     * --When--
     * --------
     */

    @When("^(.+) > Нажимаю на '(.+)'()$")
    @When("^(.+) > Нажимаю на '(.+)'( без прокрутки)$")
    @When("^(.+) > Нажимаю на '(.+)'( с прокруткой в верх)$")
    public void clickElementOnSiteByText(String siteName, String element, String scrollView) {
        element = enricher.enrich(element);
        if (scrollView.equals(" без прокрутки")) {
            findElementByName(siteName, element)
                    .click();
        } else {
            abstractFrontSteps.scrollTo(findElementByName(siteName, element), !scrollView.equals(" с прокруткой в верх"))
                    .click();
        }

        log.info("Нажал на '{}'{}", element, scrollView);
    }

    @When("^(.+) > Нажимаю на чекбокс '(.+)'$")
    public void clickCheckbox(String siteName, String text) {
        String enrichedText = enricher.enrich(text);
        BaseElement checkBox = abstractFrontSteps.onSite(siteName).findCheckBoxByName(enrichedText, abstractFrontSteps.getAtlas());
        abstractFrontSteps.scrollTo(checkBox, true).waitUntil(enabled());
        abstractFrontSteps.jsClick(checkBox);
        log.info("Нажал на чекбокс '{}' с прокруткой", enrichedText);
    }

    @When("^(.+) > Проверяю, что чекбокс '(.+)' (выбран|не выбран)$")
    public void checkCheckboxState(String siteName, String text, String expectedState) {
        String enrichedText = enricher.enrich(text);
        BaseElement checkBox = abstractFrontSteps.onSite(siteName).findCheckBoxByName(enrichedText, abstractFrontSteps.getAtlas());
        abstractFrontSteps.scrollTo(checkBox, true).waitUntil(enabled());
        String failMessage = "Неверное состояние чекбокса";
        if (expectedState.equals("выбран")) {
            Assertions.assertThat(checkBox.isSelected()).withFailMessage(failMessage).isTrue();
        } else {
            Assertions.assertThat(checkBox.isSelected()).withFailMessage(failMessage).isFalse();
        }
        log.info("Проверил, что чекбокс '{}' {}", enrichedText, expectedState);
    }

    @When("^(.+) > Ввожу текст '(.+)' в поле '(.+)'$")
    public void setText(String siteName, String text, String element) {
        text = enricher.enrich(text);
        element = enricher.enrich(element);
        abstractFrontSteps.setText(findInputByName(siteName, element), text);
        log.info("Ввел {} в - {}", text, element);
    }

    @When("^(.+) > Ввожу текст '(.+)' в поле '(.+)' с выбором$")
    public void setTextSelection(String siteName, String text, String element) {
        text = enricher.enrich(text);
        element = enricher.enrich(element);
        abstractFrontSteps.setText(findInputByName(siteName, element), text);
        abstractFrontSteps.onSite(siteName)
                .findElementByName(text)
                .clickAndSubmit();
        log.info("Ввел текст '{}' в поле '{}' с выбором", text, element);
    }

    @When("^(.+) > Получаю UUID из текущего URL$")
    public void getCurrentUrl(String site) {
        String url = abstractFrontSteps.onSite(site).getWrappedDriver().getCurrentUrl();
        String regex = ".*(.{8}-.{4}-.{4}-.{4}-.{12}).*";
        String applicationId = url.replaceAll(regex, "$1");
        Assertions.assertThat(applicationId).withFailMessage("Не удалось извечь UUID из Url")
                .matches(regex);
        AutoTestContext.put("applicationId", applicationId);
        log.info("Извлек applicationId - {} из url - {}", applicationId, url);
    }

    @When("^Обновляю страницу$")
    public void refreshCurrentPage() {
        abstractFrontSteps.getWebDriver().navigate().refresh();
        log.info("Обновил страницу");
    }

    @When("^Перехожу на вкладку с номером '(.+)'$")
    public void switchToTab(int number) {
        abstractFrontSteps.switchWindowTabOnNumber(number);
        log.info("Перешел на вкладку с номером '{}'", number);
    }

    @When("^Закрываю вкладку с номером '(.+)'$")
    public void switchToTabAndClose(int number) {
        abstractFrontSteps.switchWindowTabOnNumber(number).close();
        log.info("Закрыл вкладку с номером '{}'", number);
    }

    @When("^(.+) > Перехожу на сайт$")
    public void openSite(String siteName) {
        abstractFrontSteps.getWebDriver().get(getServiceUrl(siteName));
        log.info("Перешел на сайт - {}", siteName);
    }

    @When("^Перехожу по ссылке '(.+)'$")
    public void openUrl(String url) {
        String enrichedUrl = enricher.enrich(url);
        abstractFrontSteps.getWebDriver().get(enrichedUrl);
        log.info("Перешёл по ссылке - {}", enrichedUrl);
    }

    @When("^(.+) > Получаю ссылку и сохраняю как '(.+)'$")
    public void saveCurrentUrl(String siteName, String variableName) {
        String currentUrl = abstractFrontSteps.onSite(siteName).getWrappedDriver().getCurrentUrl();
        AutoTestContext.put(variableName, currentUrl);
        log.info("Сохраняю url {} как {}", currentUrl, variableName);
    }

    @When("^(.+) > Ожидаю что '(.+)' нет на странице$")
    public void waitUntilTextNoPresent(String siteName, String text) {
        text = enricher.enrich(text);
        BaseElement element = findElementByName(siteName, text);

        if (abstractFrontSteps.isPresent(element)) {
            abstractFrontSteps.waitUntilNotPresent(element, envSettings.getFrontLongTimeoutInSeconds());
            Assertions.assertThat(abstractFrontSteps.isPresent(element))
                    .withFailMessage(String.format("Элемент '%s' есть на странице", text))
                    .isFalse();
        }
        log.info(String.format("Успех: Элемента '%s' нет на странице", text));
    }

    @When("^(.+) > Ожидаю появления элемента с текстом '(.+)'$")
    public void waitForElementWithText(String siteName, String text) {
        String enrichedText = enricher.enrich(text);
        boolean isPresent = abstractFrontSteps.isPresent(findElementByName(siteName, enrichedText), envSettings.getFrontLongTimeoutInSeconds());
        Assertions.assertThat(isPresent).withFailMessage(String.format("Элемент с текстом '%s' не появился на странице", enrichedText))
                .isTrue();
        log.info("Элемент с текстом '{}' появился на странице", enrichedText);
    }

    @When("^(.+) > Ожидаю появления элемента содержащий текстом '(.+)'$")
    public void waitElementContainingText(String siteName, String text) {
        String enrichedText = enricher.enrich(text);
        boolean isPresent = abstractFrontSteps.isPresent(findElementContainingText(siteName, enrichedText), envSettings.getFrontLongTimeoutInSeconds());
        Assertions.assertThat(isPresent).withFailMessage(String.format("Элемент содержащий текст '%s' не появился на странице", enrichedText))
                .isTrue();
        log.info("Элемент содержащий текст '{}' появился на странице", enrichedText);
    }

    @When("^(.+) > Нажимаю на (.+) кнопку с названием '(.+)'$")
    public void clickButtonNameByNumber(String siteName, Integer number, String text) {
        findListElementsText(siteName, number - 1, enricher.enrich(text)).click();
    }

    /**
     * --------
     * --Then--
     * --------
     */

    @Then("^(.+) > Проверяю, что '(.+)' есть на странице$")
    public void checkTextOnSite(String siteName, String text) {
        String enrichedText = enricher.enrich(text);
        ConditionFactory waitSettings = waitUtils.getDefaultSettings()
                .timeout(envSettings.getFrontLongTimeoutInSeconds(), TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .alias(String.format("Ожидание появление элемента с текстом '%s'", enrichedText));
        boolean isPresent = waitUtils.waitUntil(waitSettings, () -> {
            try {
                return abstractFrontSteps.onSite(siteName).findElementsWithText().stream()
                        .anyMatch(element -> element.getText().replaceAll("\\s+", " ").contains(enrichedText));
            } catch (WebDriverException e) {
                return false;
            }
        });
        Assertions.assertThat(isPresent).withFailMessage("Не найден элемент с текстом '%s'", enrichedText).isTrue();
        log.info("Проверил, что '{}' есть на странице", enrichedText);
    }

    @Then("^(.+) > Проверяю, что на странице отображаются значения:$")
    public void checkValueOnPage(String siteName, List<String> name) {
        name.forEach((key) -> {
            String enrichedKey = enricher.enrich(key);
            abstractFrontSteps.onSite(siteName).findElementByValue(enricher.enrich(enrichedKey)).isDisplayed();
            log.info(enrichedKey + " - присутствует на странице");
        });
    }

    @Then("^(.+) > Проверяю, что на странице отображается:$")
    public void checkTextOnPage(String siteName, List<String> name) {
        name.forEach((key) -> {
            String enrichKey = enricher.enrich(key);
            abstractFrontSteps.onSite(siteName).findElementByName(enrichKey).waitUntil(displayed());
            log.info(enrichKey + " - присутствует на странице");
        });
    }

    @Then("^(.+) > Проверяю, что на странице отображается текст в количестве:$")
    public void checkTextOnPageWithNumber(String siteName, Map<String, String> params) {
        Map<String, String> enrichedParams = enricher.enrich(params);
        enrichedParams.forEach((value, count) -> {
            String size = String.valueOf(abstractFrontSteps.onSite(siteName).findElements(By.xpath(".//*[text()= '" + value + "']")).size());
            Assertions.assertThat(size)
                    .withFailMessage(String.format("Ожидалось '%s' значений '%s', а отображается '%s'", count, value, size))
                    .isEqualTo(count);
        });
    }

    @Then("^(.+) > Проверяю, что на странице не отображается:$")
    public void checkTextNotDisplayedOnSite(String siteName, List<String> name) {
        name.forEach((key) -> isTextNotDisplayedOnSite(siteName, enricher.enrich(key)));
    }

    @Then("^(.+) > Проверяю, что элемент '(.+)' с текстом '(.+)' есть на странице$")
    public void checkElementOnSite(String siteName, String element, String text) {
        element = enricher.enrich(element);
        text = enricher.enrich(text);
        abstractFrontSteps.onSite(siteName)
                .findElementByAttributeAndName(element, text)
                .waitUntil(displayed());
        log.info("Проверил, что элемент {} с текстом {} есть на странице", element, text);
    }

    @Then("^(.+) > Проверяю, что '(.+)' нет на странице$")
    public void checkNotDisplayedTextOnSite(String siteName, String name) {
        isTextNotDisplayedOnSite(siteName, name);
    }

    @Then("^(.+) > Проверяю, что кнопка '(.+)' активна$")
    public void checkActiveButtonOnSite(String siteName, String element) {
        element = enricher.enrich(element);
        findButtonByName(siteName, element).should(enabled());
        log.info("Проверил, что кнопка {} активна", element);
    }

    @Then("^(.+) > Проверяю, что поле '(.+)' не активно$")
    public void checkActiveFields(String siteName, String element) {
        element = enricher.enrich(element);
        findInputByName(siteName, element).should(not(enabled()));
        log.info("Проверил, что поле '{}' не активно", element);
    }

    @Then("^(.+) > Проверяю, что кнопка '(.+)' не активна$")
    public void checkNotActiveButtonOnSite(String siteName, String element) {
        element = enricher.enrich(element);
        findButtonByName(siteName, element).should(not(enabled()));
        log.info("Проверил, что кнопка {} не активна", element);
    }

    private String getServiceUrl(String serviceName) {
        return serviceConfiguration.getService(serviceName).getUrl();
    }

    private BaseElement findElementByName(String siteName, String element) {
        return abstractFrontSteps.onSite(siteName).findElementByName(element);
    }

    private BaseElement findListElementsText(String siteName, Integer number, String element) {
        return abstractFrontSteps.onSite(siteName).findListElementsText(element).get(number);
    }

    private BaseElement findElementContainingText(String siteName, String element) {
        return abstractFrontSteps.onSite(siteName).findElementByContainsName(element);
    }

    private InputTextElement findInputByName(String siteName, String element) {
        return abstractFrontSteps.onSite(siteName).findInputByName(element);
    }

    private BaseElement findButtonByName(String siteName, String element) {
        return abstractFrontSteps.onSite(siteName).findButtonByName(element);
    }

    private void isTextNotDisplayedOnSite(String siteName, String name) {
        String enrichedName = enricher.enrich(name);
        Assertions.assertThat(abstractFrontSteps.isPresent(abstractFrontSteps.onSite(siteName).findElementByName(enrichedName)))
                .withFailMessage("Элемент - '" + enrichedName + "' найден на странице")
                .isFalse();
        log.info(enrichedName + " - отсутствует на странице");
    }
}