package dev.kozhevnikov.autotests.web.pages.saucedemo;

import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;

public interface CheckoutPage extends AbstractPage {

    @Name("Имя")
    @FindBy("//input[@data-test='firstName']")
    BaseElement firstName();

    @Name("Фамилия")
    @FindBy("//input[@data-test='lastName']")
    BaseElement lastName();

    @Name("Почтовый индекс")
    @FindBy("//input[@data-test='postalCode']")
    BaseElement postalCode();

    @Name("Кнопка продолжить")
    @FindBy("//input[@data-test='continue']")
    BaseElement continueButton();

    @Name("Кнопка завершить")
    @FindBy("//button[@data-test='finish']")
    BaseElement finish();

    @Name("Заголовок подтверждения заказа")
    @FindBy("//h2[@data-test='complete-header']")
    BaseElement completeHeader();
}
