package dev.kozhevnikov.autotests.utils.startdata.partners.factory;

import dev.kozhevnikov.autotests.utils.startdata.partners.config.PartnersConfig;

import java.util.List;

public interface PartnersFactory {
    List<PartnersConfig.Partner> create();
}
