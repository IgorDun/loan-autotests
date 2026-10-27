package dev.kozhevnikov.autotests.web.pages.saucedemo;

import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;

public interface CartPage extends AbstractPage {

    @Name("Кнопка оформления заказа")
    @FindBy("//button[@data-test='checkout']")
    BaseElement checkout();
}
