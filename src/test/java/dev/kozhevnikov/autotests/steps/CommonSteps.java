package dev.kozhevnikov.autotests.steps;

import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.kozhevnikov.autotests.config.CucumberConfiguration;
import dev.kozhevnikov.autotests.utils.common.AutoTestContext;
import dev.kozhevnikov.autotests.utils.common.Enricher;
import dev.kozhevnikov.autotests.utils.common.Evaluator;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CommonSteps extends CucumberConfiguration {
    private final Enricher enricher;
    private final Evaluator evaluator;

    /**
     * --------
     * --When--
     * --------
     */

    @When("Обрабатываю строку {string} с помощью groovy и сохраняю как {string}:")
    public void handleStringByGroovy(String stringName, String resultStringName, List<String> commands) {
        String string = (String) AutoTestContext.getNotNull(stringName);
        List<String> enrichedCommands = enricher.enrich(commands);

        for (String command : enrichedCommands) {
            Map<String, Object> args = Map.of(stringName, string);
            string = evaluator.evaluate(command, String.class, args);
        }

        AutoTestContext.put(resultStringName, string);
        log.info("Результат обработки: {}", string);
    }
}
