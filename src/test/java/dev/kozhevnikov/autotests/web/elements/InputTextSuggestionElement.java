package dev.kozhevnikov.autotests.web.elements;

import io.qameta.atlas.webdriver.ElementsCollection;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;

public interface InputTextSuggestionElement extends InputTextElement, BaseElement {

    @Name("Список вариантов")
    @FindBy(value = "//button[@data-class='SuggestionsListItem'] " +
            "| //*[@class='ant-select-item-option-content'] " +
            "| //*[contains(@data-component,'OptionsItem')]" +
            "| //*[contains(@data-component,'OptionsItem')]//span")
    ElementsCollection<BaseElement> suggestionsList();


    @Override
    @Deprecated
    /**
     * @deprecated
     * Метод не используется. Следует пользоваться методом {@link dev.kozhevnikov.autotests.steps.front.AbstractFrontSteps#setText(InputTextElement, String)}
     */
    default void setText(String text) {
        throw new UnsupportedOperationException("Операция ввода текста в InputTextSuggestionElement прямым вызовом не поддерживается.");
    }
}