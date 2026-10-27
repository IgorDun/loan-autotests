package dev.kozhevnikov.autotests.web.pages;

import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;
import io.qameta.atlas.webdriver.extension.Param;
import dev.kozhevnikov.autotests.web.elements.BaseElement;

public interface FileUploaderPage extends AbstractPage {

    @Name("Поле ввода для файла - {{ file }}")
    @FindBy(("//*[*='{{ file }}']//*[@type='file'] " +
            "| //*[@id='accessApplication' and @type='file'] " +
            "| //*[text()='{{ file }}']//following::input[@type='file']"))
    BaseElement findFileInputByName(@Param("file") String file);
}
