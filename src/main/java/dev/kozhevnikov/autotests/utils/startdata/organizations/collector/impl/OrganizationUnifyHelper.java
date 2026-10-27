package dev.kozhevnikov.autotests.utils.startdata.organizations.collector.impl;

import dev.kozhevnikov.autotests.utils.startdata.organizations.config.OrganizationsConfig.Accreditation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Дефолты аккредитаций, чтобы не повторять их в YAML: тип берётся из признака online организации,
 * для онлайн-аккредитации способ выдачи товара — OFFICE.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrganizationUnifyHelper {

    public static List<Accreditation> unifyAccreditations(Boolean online, List<Accreditation> accreditations) {
        String defaultType = Boolean.TRUE.equals(online) ? "ONLINE" : "OFFLINE";

        return Optional.ofNullable(accreditations)
                .orElseGet(Collections::emptyList)
                .stream()
                .peek(accreditation -> {
                    if (accreditation.getType() == null) {
                        accreditation.setType(defaultType);
                    }
                    if (accreditation.getGoodsDelivery() == null && accreditation.getType().equalsIgnoreCase("ONLINE")) {
                        accreditation.setGoodsDelivery("OFFICE");
                    }
                })
                .collect(Collectors.toList());
    }
}
