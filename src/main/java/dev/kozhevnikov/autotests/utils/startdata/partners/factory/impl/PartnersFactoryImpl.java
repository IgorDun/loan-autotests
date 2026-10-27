package dev.kozhevnikov.autotests.utils.startdata.partners.factory.impl;

import dev.kozhevnikov.autotests.utils.startdata.partners.collector.PartnersCollector;
import dev.kozhevnikov.autotests.utils.startdata.partners.config.PartnersConfig.Partner;
import dev.kozhevnikov.autotests.utils.startdata.partners.factory.PartnersFactory;
import dev.kozhevnikov.autotests.utils.startdata.utility.PartnerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Партнёр ищется по ИНН и КПП: найден — обновляется данными из YAML, не найден — создаётся.
 */
@Component
@RequiredArgsConstructor
public class PartnersFactoryImpl implements PartnersFactory {
    private final PartnersCollector partnersCollector;
    private final PartnerUtils partnerUtils;

    @Override
    public List<Partner> create() {
        return partnersCollector.collect()
                .parallelStream()
                .map(this::createOrUpdatePartner)
                .toList();
    }

    private Partner createOrUpdatePartner(Partner partner) {
        Map<String, String> info = partner.getInfo();
        String id = partnerUtils.findPartnerIdByInnAndKpp(info.get("inn"), info.get("kpp"))
                .map(partnerId -> partnerUtils.updatePartnerAndGetId(partnerId, info))
                .orElseGet(() -> partnerUtils.createPartnerAndGetId(info));
        info.put("id", id);
        return partner;
    }
}
