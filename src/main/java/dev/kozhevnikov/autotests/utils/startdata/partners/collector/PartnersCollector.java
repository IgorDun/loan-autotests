package dev.kozhevnikov.autotests.utils.startdata.partners.collector;

import dev.kozhevnikov.autotests.utils.startdata.partners.config.PartnersConfig;

import java.util.List;

public interface PartnersCollector {
    List<PartnersConfig.Partner> collect();
}
