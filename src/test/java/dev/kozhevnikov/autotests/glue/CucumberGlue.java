package dev.kozhevnikov.autotests.glue;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import dev.kozhevnikov.autotests.utils.startdata.StartDataConfig;
import dev.kozhevnikov.autotests.api.model.ServiceConfig;
import dev.kozhevnikov.autotests.api.model.http.AtHttpRequest;
import dev.kozhevnikov.autotests.config.EnvSettings;
import dev.kozhevnikov.autotests.utils.startdata.facade.StartData;
import dev.kozhevnikov.autotests.utils.api.JsonUtils;
import dev.kozhevnikov.autotests.utils.api.ServiceUtils;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CucumberGlue {

    private final EnvSettings envSettings;
    private final JsonUtils jsonUtils;
    private final ServiceUtils serviceUtils;
    private final StartDataConfig startDataConfig;
    private final StartData startData;
    private final ServiceConfig wiremockService;


    private static volatile boolean testDataGenerated = false;

    @Before(order = 0)
    public void logEnv() {
        log.info("Среда - {}", envSettings.getInventoryName());
    }

    @Before(order = 1)
    public synchronized void generateTestData() {
        if (!startDataConfig.needCreate() || testDataGenerated) {
            return;
        }
        testDataGenerated = true;
        log.info("Settings:\n{}", jsonUtils.prettify(startData.getSettings()));
    }

    @After(order = 0)
    @SuppressWarnings("unchecked")
    public void clearTestContext() {
        if (AutoTestContext.containsKey("mappings")) {
            ((List<String>) AutoTestContext.get("mappings")).forEach(mapping -> {
                AtHttpRequest request = serviceUtils.generateHttpRequest(wiremockService).resolveUri(mapping).setMethod(HttpMethod.DELETE);
                serviceUtils.sendHttpRequest(request);
                log.info("Удалена заглушка, id - {}", mapping);
            });
        }

        AutoTestContext.clearContext();
        log.info("Контекст очищен");
    }
}