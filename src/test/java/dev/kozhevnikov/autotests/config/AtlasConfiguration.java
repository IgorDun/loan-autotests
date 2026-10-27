package dev.kozhevnikov.autotests.config;

import io.qameta.atlas.core.Atlas;
import io.qameta.atlas.core.api.MethodExtension;
import io.qameta.atlas.core.context.RetryerContext;
import io.qameta.atlas.core.internal.Configuration;
import io.qameta.atlas.core.internal.DefaultRetryer;
import io.qameta.atlas.core.internal.Retryer;
import io.qameta.atlas.webdriver.context.WebDriverContext;
import io.qameta.atlas.webdriver.extension.DriverProviderExtension;
import io.qameta.atlas.webdriver.extension.ExecuteJScriptMethodExtension;
import io.qameta.atlas.webdriver.extension.FindByCollectionExtension;
import io.qameta.atlas.webdriver.extension.FindByExtension;
import io.qameta.atlas.webdriver.extension.PageExtension;
import io.qameta.atlas.webdriver.extension.ShouldMethodExtension;
import io.qameta.atlas.webdriver.extension.WaitUntilMethodExtension;
import io.qameta.atlas.webdriver.extension.WrappedElementMethodExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import dev.kozhevnikov.autotests.web.extension.DefaultMethodExtension;
import dev.kozhevnikov.autotests.web.extension.SelectExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@org.springframework.context.annotation.Configuration
@ConditionalOnClass({Atlas.class, WebDriverContext.class})
@SuppressWarnings("unused")
public class AtlasConfiguration {

    @Bean
    @Lazy
    public DriverProviderExtension driverProviderExtension() {
        return new DriverProviderExtension();
    }

    @Bean
    @Lazy
    public DefaultMethodExtension defaultMethodExtension() {
        return new DefaultMethodExtension();
    }

    @Bean
    @Lazy
    public SelectExtension selectExtension() {
        return new SelectExtension();
    }

    @Bean
    @Lazy
    public FindByExtension findByExtension() {
        return new FindByExtension();
    }

    @Bean
    @Lazy
    public FindByCollectionExtension findByCollectionExtension() {
        return new FindByCollectionExtension();
    }

    @Bean
    @Lazy
    public ShouldMethodExtension shouldMethodExtension() {
        return new ShouldMethodExtension();
    }

    @Bean
    @Lazy
    public WaitUntilMethodExtension waitUntilMethodExtension() {
        return new WaitUntilMethodExtension();
    }

    @Bean
    @Lazy
    public WrappedElementMethodExtension wrappedElementMethodExtension() {
        return new WrappedElementMethodExtension();
    }

    @Bean
    @Lazy
    public ExecuteJScriptMethodExtension executeJScriptMethodExtension() {
        return new ExecuteJScriptMethodExtension();
    }

    @Bean
    @Lazy
    public PageExtension pageExtension() {
        return new PageExtension();
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public Retryer retryer(EnvSettings envSettings) {
        long timeout = envSettings.getFrontTimeoutInSeconds();
        return new DefaultRetryer(TimeUnit.SECONDS.toMillis(timeout), 1000L, Collections.singletonList(Throwable.class));
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public RetryerContext retryerContext(Retryer retryer) {
        return new RetryerContext(retryer);
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public WebDriverContext webDriverContext(WebDriver webDriver) {
        return new WebDriverContext(webDriver);
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public Configuration configuration(WebDriverContext webDriverContext, Collection<MethodExtension> extensions) {
        Configuration configuration = new Configuration();
        configuration.registerContext(webDriverContext);
        extensions.forEach(configuration::registerExtension);
        return configuration;
    }

    @Bean
    @Lazy
    @Scope("cucumber-glue")
    public Atlas atlas(Configuration configuration, RetryerContext retryerContext) {
        Atlas atlas = new Atlas(configuration);
        atlas.context(retryerContext);
        return atlas;
    }
}
