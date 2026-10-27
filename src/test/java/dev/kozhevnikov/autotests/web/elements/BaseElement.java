package dev.kozhevnikov.autotests.web.elements;

import io.qameta.atlas.core.Atlas;
import io.qameta.atlas.webdriver.AtlasWebElement;
import io.qameta.atlas.webdriver.ElementsCollection;
import io.qameta.atlas.webdriver.extension.DriverProvider;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;
import io.qameta.atlas.webdriver.extension.Param;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.Arrays;

public interface BaseElement extends AtlasWebElement<WebElement>, WrapsDriver {

    @Override
    @DriverProvider
    WebDriver getWrappedDriver();

    @Name("Поиск всех элементов с текстом")
    @FindBy(".//*[text()]")
    ElementsCollection<BaseElement> findElementsWithText();

    @Name("Поиск элемента по атрибуту - {{ attribute }}")
    @FindBy(".//*[@{{ attribute }} = '{{ name }}']")
    BaseElement findElementByAttributeAndName(@Param("attribute") String attribute, @Param("name") String name);

    @Name("Окно ввода - {{ name }}")
    @FindBy((".//*[text()= '{{ name }}']/../input"))
    InputTextElement findInputByName(@Param("name") String name);

    @Name("Кнопка - {{ name }}")
    @FindBy((".//*[text()= '{{ name }}']/ancestor::button"))
    BaseElement findButtonByName(@Param("name") String name);

    @Name("Элемент - {{ name }}")
    @FindBy((".//*[text()= '{{ name }}']"))
    BaseElement findElementByName(@Param("name") String name);

    @Name("Элементы - {{ name }}")
    @FindBy((".//*[text()= '{{ name }}']"))
    ElementsCollection<BaseElement> findListElementsText(@Param("name") String name);

    @Name("Часть '{{ text }}' текста элемента")
    @FindBy((".//*[contains(text(),'{{ text }}')]"))
    BaseElement findElementByContainsName(@Param("text") String text);

    @Name("Значение элемент - {{ name }}")
    @FindBy((".//*[@value= '{{ name }}']"))
    BaseElement findElementByValue(@Param("name") String name);

    @Name("Надпись - {{ name }}")
    @FindBy((".//span[text()= '{{ name }}']"))
    BaseElement findSpanByName(@Param("name") String name);

    default SelectableElement findCheckBoxByName(String name, Atlas atlas) {
        WebElement label = findElements(By.xpath(".//label")).stream()
                .filter(element -> element.getText().startsWith(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("Не найдено элементов с текстом %s", name)));
        WebElement checkbox = label.findElement(By.xpath(".//input[@type='checkbox']"));
        return atlas.create(checkbox, SelectableElement.class);
    }

    @Override
    default boolean isSelected() {
        WebElement parent = this.findElement(By.xpath(".//parent::*"));
        return Boolean.parseBoolean(this.getAttribute("checked"))
                || Boolean.parseBoolean(this.getAttribute("selected"))
                || Boolean.parseBoolean(this.getAttribute("data-checked"))
                || Boolean.parseBoolean(parent.getAttribute("data-checked"))
                || Boolean.parseBoolean(parent.getAttribute("data-selected"));
    }

    @SneakyThrows
    default Object getElementByAnnotationName(String name) {
        return Arrays.stream(this.getClass().getInterfaces())
                .flatMap(iface -> Arrays.stream(iface.getMethods()).filter(method -> method.isAnnotationPresent(Name.class)))
                .filter(method -> method.getAnnotation(Name.class).value().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найден объект с @Name(" + name + ")"))
                .invoke(this);
    }

    @SneakyThrows
    default Object getElementByAnnotationNameIndex(String name, String index) {
        return Arrays.stream(this.getClass().getInterfaces())
                .flatMap(iface ->
                        Arrays.stream(iface.getMethods()).filter(method ->
                                method.isAnnotationPresent(Name.class)))
                .filter(method ->
                        method.getAnnotation(Name.class).value().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найден объект с @Name(" + name + ")"))
                .invoke(this, index);
    }

    default boolean isDisplayed(String text) {
        try {
            return this.findElementByName(text).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    default BaseElement clickAndSubmit() {
        this.click();
        new Actions(getWrappedDriver()).click().perform();
        return this;
    }

    default WebElement clearValue() {
        WebElement webElement = this.findElement(By.xpath("*"));
        while (webElement.getAttribute("value").length() > 0) {
            webElement.sendKeys(Keys.chord(Keys.BACK_SPACE));
        }
        return webElement;
    }

    default BaseElement focusElement() {
        new Actions(getWrappedDriver())
                .moveToElement(this)
                .click()
                .perform();
        return this;
    }
}
