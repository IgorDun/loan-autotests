package dev.kozhevnikov.autotests.web.sites.front;

import io.qameta.atlas.webdriver.WebSite;
import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.pages.AbstractPage;

import java.util.Arrays;

public interface AbstractSite extends WebSite, BaseElement {

    @SuppressWarnings("unchecked")
    default <T extends AbstractPage> T onPage(Class<T> pageType) {
        try {
            return  (T) Arrays.stream(getClass().getMethods())
                    .filter(method -> method.getReturnType().equals(pageType))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Не найден метод для вызова страницы " + pageType.getName()))
                    .invoke(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
