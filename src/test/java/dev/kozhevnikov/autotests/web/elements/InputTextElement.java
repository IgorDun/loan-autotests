package dev.kozhevnikov.autotests.web.elements;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;

public interface InputTextElement extends BaseElement {

    default String getText() {
        return this.getAttribute("value");
    }

    default void clear() {
        while (this.getText().length() > 0) {
            this.sendKeys(Keys.chord(Keys.BACK_SPACE));
        }
    }

    default void setText(String text) {
        this.clear();
        if (!StringUtils.isEmpty(text)) {
            this.sendKeys(text);
        }

    }

    @Deprecated
    /**
     * @deprecated
     * Метод не используется. Следует пользоваться методом {@link dev.kozhevnikov.autotests.steps.front.AbstractFrontSteps#replaceInputValue(InputTextElement, String)}
     */
    default void replaceValue(String text) {
        throw new UnsupportedOperationException("Операция замены текста в InputTextElement прямым вызовом не поддерживается.");
    }
}
