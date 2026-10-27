package dev.kozhevnikov.autotests;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Точка запуска сценариев через JUnit Platform.
 * Плагины, фильтр тегов и параллелизм задаются в junit-platform.properties
 * или из командной строки: -Dcucumber.filter.tags="@smoke".
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "dev.kozhevnikov.autotests.steps,dev.kozhevnikov.autotests.glue,dev.kozhevnikov.autotests.hooks,dev.kozhevnikov.autotests.config")
public class RunCucumberTest {
}
