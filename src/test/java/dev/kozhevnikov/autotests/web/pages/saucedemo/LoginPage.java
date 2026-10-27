package dev.kozhevnikov.autotests.web.pages.saucedemo;

import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;

public interface LoginPage extends AbstractPage {

    @Name("Поле логина")
    @FindBy("//input[@id='user-name']")
    BaseElement userName();

    @Name("Поле пароля")
    @FindBy("//input[@id='password']")
    BaseElement password();

    @Name("Кнопка входа")
    @FindBy("//input[@id='login-button']")
    BaseElement loginButton();

    @Name("Сообщение об ошибке")
    @FindBy("//*[@data-test='error']")
    BaseElement error();
}
