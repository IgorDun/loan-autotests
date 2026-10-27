package dev.kozhevnikov.autotests.web.sites.front;

/**
 * Пример сайта: открытый тренажёр https://www.saucedemo.com.
 * Каждый фронт описывается интерфейсом-наследником AbstractSite, имя регистрируется
 * в {@link dev.kozhevnikov.autotests.web.sites.Sites} и в services.properties (базовый URL).
 * Общие шаги обращаются к сайту по имени: "saucedemo > Перехожу на сайт".
 * Методы с @Page на сайте открывают базовый URL при каждом вызове, поэтому страницы
 * внутри сценария создаются через AbstractFrontSteps#currentPage без навигации.
 */
public interface SauceDemoSite extends AbstractSite {
}
