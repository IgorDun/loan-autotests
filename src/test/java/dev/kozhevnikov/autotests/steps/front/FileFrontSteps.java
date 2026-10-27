package dev.kozhevnikov.autotests.steps.front;

import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.kozhevnikov.autotests.web.pages.FileUploaderPage;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.FileUtils;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FileFrontSteps {
    private final AbstractFrontSteps abstractFrontSteps;
    private final Enricher enricher;
    private final FileUtils fileUtils;

    /**
     * --------
     * --When--
     * --------
     */

    @When("^(.+) > Прикрепляю (временный|созданный ранее) файл для загрузки:$")
    public void loadPhotoFile(String site, String fileType, Map<String, String> params) {
        boolean isTempValue = fileType.equals("временный");
        params.forEach((key, value) -> {
            String convertedKey = key.replace("\\s", " ");
            String enrichedValue = enricher.enrich(value);
            String absolutePath = isTempValue ? enrichedValue : fileUtils.getResourceFile(enrichedValue).getAbsolutePath();
            abstractFrontSteps.onSite(site)
                    .onPage(FileUploaderPage.class)
                    .findFileInputByName(convertedKey)
                    .sendKeys(absolutePath);
            log.info("Загружен файл - {}", enrichedValue);
        });
    }
}

