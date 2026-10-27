package dev.kozhevnikov.autotests.web.pages.modals;

import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;
import dev.kozhevnikov.autotests.web.elements.BaseElement;
import dev.kozhevnikov.autotests.web.elements.TableElement;

public interface Modal extends BaseElement {

    @Name("Кнопка закрыть")
    @FindBy(value = "//div[@data-type='close'] | //button[@aria-label='Close'] | .//div[@data-type='IconClose']")
    BaseElement close();

    @Name("Таблица в модальном окне")
    @FindBy(value = ".//*[@class='ant-modal-body']")
    TableElement tableModalWindow();
}
