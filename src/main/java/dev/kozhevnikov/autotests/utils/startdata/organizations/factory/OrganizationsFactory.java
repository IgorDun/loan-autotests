package dev.kozhevnikov.autotests.utils.startdata.organizations.factory;

import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig;

import java.util.List;

public interface OrganizationsFactory {
    List<OrganizationsConfig.Organization> create();
}
