package dev.kozhevnikov.autotests.web.pages.saucedemo;

import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;
import io.qameta.atlas.webdriver.extension.Param;

public interface InventoryPage extends AbstractPage {

    @Name("Заголовок страницы")
    @FindBy("//span[@data-test='title']")
    BaseElement title();

    @Name("Кнопка добавления в корзину товара - {{ product }}")
    @FindBy("//div[@data-test='inventory-item'][.//div[@data-test='inventory-item-name' and text()='{{ product }}']]//button")
    BaseElement addToCart(@Param("product") String product);

    @Name("Счётчик корзины")
    @FindBy("//span[@data-test='shopping-cart-badge']")
    BaseElement cartBadge();

    @Name("Корзина")
    @FindBy("//a[@data-test='shopping-cart-link']")
    BaseElement cartLink();
}
