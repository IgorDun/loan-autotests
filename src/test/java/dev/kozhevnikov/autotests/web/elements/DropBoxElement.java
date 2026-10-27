package dev.kozhevnikov.autotests.web.elements;

import io.qameta.atlas.webdriver.AtlasWebElement;
import io.qameta.atlas.webdriver.ElementsCollection;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;

import java.util.function.Predicate;

public interface DropBoxElement extends BaseElement {

    @Name("Имя списка")
    @FindBy(value = ".//span[@data-component='PopoverOpener']//label | //*[@role='listbox' and contains(@style ,'hidden')]")
    BaseElement name();

    @Name("Текущее значение")
    @FindBy(value = ".//span[@data-component='PopoverOpener']//a | .")
    BaseElement currentValue();

    @Name("Список возможных значений")
    @FindBy(value = "//div[@data-component='PopoverBox']//div[@data-component='OptionsItem'] " +
            "| //*[contains(@data-testid,'product/')]" +
            "| //*[contains(@data-testid,'importFiles/files/mode/')]" +
            "| //*[contains(@data-testid,'organization/filter/category/')]" +
            "| //*[contains(@data-testid,'organization/filter_')]" +
            "| //*[contains(@data-testid,'banks/filter_')]" +
            "| //*[contains(@class,'rc-virtual-list')]//div[@title]" +
            "| //div[@data-component='InputOptionsItem']")
    ElementsCollection<BaseElement> valuesList();

    default void selectValue(String text, Predicate<String> textFilter) {
        currentValue().click();
        valuesList().stream()
                .filter(element -> textFilter.test(element.getText()))
                .findFirst()
                .ifPresentOrElse(AtlasWebElement::click,
                        () -> {
                            throw new RuntimeException(String.format("Не найдено значение '%s' в списке '%s'", text, name().getText()));
                        }
                );
    }

    default boolean hasValue(String text) {
        currentValue().click();
        return valuesList().stream().anyMatch(element -> element.getText().equals(text));
    }
}
