package dev.kozhevnikov.autotests.utils.startdata.utility;

import java.util.Map;
import java.util.Optional;

public interface PartnerUtils {
    String findPartnerIdByCode(String partnerCode);

    Optional<String> findPartnerIdByInnAndKpp(String inn, String kpp);

    String createPartnerAndGetId(Map<String, String> partnerInfo);

    String updatePartnerAndGetId(String partnerId, Map<String, String> partnerInfo);
}
