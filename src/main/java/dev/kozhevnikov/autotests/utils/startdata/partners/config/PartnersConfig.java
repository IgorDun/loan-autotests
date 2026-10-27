package dev.kozhevnikov.autotests.utils.startdata.partners.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Структура partners.yaml: группы (ключи команд) со списком партнёров.
 * Партнёр — внешняя организация, у которой аккредитуются точки и сотрудники.
 */
@Getter
@Setter
@Accessors(chain = true)
public class PartnersConfig {
    private Map<String, List<Partner>> partners;

    @Data
    @Accessors(chain = true)
    public static class Partner {
        /** code, name, nameOfficial, inn, kpp, bik, category */
        private Map<String, String> info;
    }
}
