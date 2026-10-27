package dev.kozhevnikov.autotests.steps.front;

import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.web.pages.saucedemo.CartPage;
import dev.kozhevnikov.autotests.web.pages.saucedemo.CheckoutPage;
import dev.kozhevnikov.autotests.web.pages.saucedemo.InventoryPage;
import dev.kozhevnikov.autotests.web.pages.saucedemo.LoginPage;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static dev.kozhevnikov.autotests.web.matchers.WebElementMatchers.displayed;
import static dev.kozhevnikov.autotests.web.matchers.WebElementMatchers.hasText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * Шаги для примера на открытом тренажёре saucedemo.com.
 * Показывают, как пишутся шаги поверх страниц Atlas для конкретного фронта.
 */
@Slf4j
@RequiredArgsConstructor
public class SauceDemoSteps {
    private final AbstractFrontSteps abstractFrontSteps;
    private final Enricher enricher;

    @When("^saucedemo > Авторизуюсь как '(.+)' с паролем '(.+)'$")
    public void login(String user, String password) {
        LoginPage page = page(LoginPage.class);
        page.userName().waitUntil(displayed()).sendKeys(enricher.enrich(user));
        page.password().sendKeys(enricher.enrich(password));
        page.loginButton().click();
        log.info("Выполнен вход пользователем '{}'", user);
    }

    @Then("^saucedemo > Вижу каталог товаров$")
    public void inventoryIsShown() {
        page(InventoryPage.class).title().should(hasText(equalTo("Products")));
    }

    @Then("^saucedemo > Вижу ошибку входа, содержащую '(.+)'$")
    public void loginErrorIsShown(String text) {
        page(LoginPage.class).error().should(hasText(containsString(enricher.enrich(text))));
    }

    @When("^saucedemo > Добавляю в корзину товар '(.+)'$")
    public void addToCart(String product) {
        page(InventoryPage.class).addToCart(enricher.enrich(product)).click();
        log.info("Товар '{}' добавлен в корзину", product);
    }

    @Then("^saucedemo > Проверяю, что в корзине (\\d+) (?:товар|товара|товаров)$")
    public void cartCount(int count) {
        page(InventoryPage.class).cartBadge().should(hasText(equalTo(String.valueOf(count))));
    }

    @When("^saucedemo > Перехожу к оформлению заказа$")
    public void openCheckout() {
        page(InventoryPage.class).cartLink().click();
        page(CartPage.class).checkout().click();
        page(CheckoutPage.class).firstName().waitUntil(displayed());
        log.info("Открыта форма оформления заказа");
    }

    @When("^saucedemo > Подтверждаю заказ$")
    public void confirmOrder() {
        CheckoutPage page = page(CheckoutPage.class);
        page.continueButton().click();
        page.finish().waitUntil(displayed()).click();
        log.info("Заказ подтверждён");
    }

    @Then("^saucedemo > Вижу подтверждение заказа '(.+)'$")
    public void orderCompleted(String text) {
        page(CheckoutPage.class).completeHeader().should(hasText(equalTo(enricher.enrich(text))));
    }

    private <T extends AbstractPage> T page(Class<T> pageType) {
        return abstractFrontSteps.currentPage(pageType);
    }
}
