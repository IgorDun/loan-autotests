package dev.kozhevnikov.autotests.utils.startdata.organizations.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Структура organizations.yaml: группы (ключи команд) со списком организаций.
 */
@Getter
@Setter
@Accessors(chain = true)
public class OrganizationsConfig {
    private Map<String, List<Organization>> organizations;

    @Data
    @Accessors(chain = true)
    public static class Organization {
        /** category, externalCode, inn, name, nameOfficial, regDate, online */
        private Map<String, String> info;
        private List<Address> addresses;
        private List<Accreditation> accreditations;
        /** externalCode дочерних организаций: они должны быть созданы раньше родителя */
        private Set<String> childOrgExternalCodes;
    }

    @Data
    @Accessors(chain = true)
    public static class Address {
        private String type;
        private String inline;
        private String regionCode;
    }

    @Data
    @Accessors(chain = true)
    public static class Accreditation {
        /** код партнёра из partners.yaml */
        private String partner;
        /** код организации в системе партнёра */
        private String extCode;
        /** ONLINE | OFFLINE, по умолчанию из info.online */
        private String type;
        /** способ выдачи товара, для ONLINE по умолчанию OFFICE */
        private String goodsDelivery;
    }
}
