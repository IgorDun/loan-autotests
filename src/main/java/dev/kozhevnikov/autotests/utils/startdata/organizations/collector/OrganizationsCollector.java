package dev.kozhevnikov.autotests.utils.startdata.organizations.collector;

import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig;

import java.util.List;

public interface OrganizationsCollector {
    List<OrganizationsConfig.Organization> collect();
}
