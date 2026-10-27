package dev.kozhevnikov.autotests.web.elements;

public interface SelectableElement extends BaseElement {

    default void select(boolean condition) {
        if (condition && !isSelected() || !condition && isSelected()) {
            click();
        }
    }

    default void selectVerification(boolean condition) {
        if (!condition) {
            if (this.getAttribute("class").contains("checked")) {
                this.click();
            }
        } else {
            if (!this.getAttribute("class").contains("checked")) {
                this.click();
            }
        }
    }
}
