package dev.kozhevnikov.autotests.web.sites;


import dev.kozhevnikov.autotests.web.sites.front.AbstractSite;
import dev.kozhevnikov.autotests.web.sites.front.SauceDemoSite;

import java.util.Arrays;

public enum Sites {

    SAUCE_DEMO("saucedemo", SauceDemoSite.class);

    private final String name;
    private final Class<? extends AbstractSite> clazz;

    Sites(String name, Class<? extends AbstractSite> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return String.format("name - %s, class - %s", name, clazz);
    }

    public String getName() {
        return name;
    }

    public static Class<? extends AbstractSite> filterSite(String name) {
        return Arrays.stream(Sites.values())
                .filter(site -> site.name.equals(name))
                .findFirst()
                .map(site -> site.clazz)
                .orElseThrow(() -> new RuntimeException("сайт не найден"));
    }

    public static String filterSite(Class<? extends AbstractSite> siteClass) {
        return Arrays.stream(Sites.values())
                .filter(site -> site.clazz.equals(siteClass))
                .findFirst()
                .map(site -> site.name)
                .orElseThrow(() -> new RuntimeException("сайт не найден"));
    }
}
