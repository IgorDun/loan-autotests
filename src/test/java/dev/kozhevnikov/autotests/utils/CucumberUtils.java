package dev.kozhevnikov.autotests.utils;

import io.cucumber.datatable.DataTable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import dev.kozhevnikov.autotests.utils.common.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Lazy
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CucumberUtils {
    private final CommonUtils commonUtils;

    public List<List<String>> filterRows(List<List<String>> rows, Predicate<List<String>> filter) {
        return rows.stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> generateMapFromRows(List<List<String>> rows,
                                                         Function<List<String>, String> keyFunc,
                                                         Function<List<String>, List<String>> valueFunc) {
        return rows.stream().collect(Collectors.toMap(keyFunc, valueFunc));
    }

    public List<Map<String, String>> asMaps(DataTable table) {
        return asMaps(table, KeyDisposition.HORIZONTAL);
    }

    public static <K, V> Map<K, V> asMap(DataTable table, Class<K> keyClass, Class<V> valueClass) {
        return table.asMap(keyClass, valueClass).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> keyClass.cast(entry.getKey()),
                        entry -> valueClass.cast(entry.getValue()))
                );
    }

    public List<Map<String, String>> asMaps(DataTable table, KeyDisposition keyDisposition) {
        switch (keyDisposition) {
            case VERTICAL:
                return asMapsWithVerticalKeys(table);
            case HORIZONTAL:
                return asMapsWithHorizontalKeys(table);
            default:
                throw new RuntimeException("Unprocessed value '" + keyDisposition + "'");
        }
    }

    private List<Map<String, String>> asMapsWithVerticalKeys(DataTable table) {
        List<Map<String, String>> result = new ArrayList<>();

        List<String> keys = table.column(0);
        for (int i = 1; i < table.width(); i++) {
            result.add(commonUtils.joinListsToMap(keys, table.column(i)));
        }

        return result;
    }

    private List<Map<String, String>> asMapsWithHorizontalKeys(DataTable table) {
        return table.asMaps()
                .stream()
                .map(map -> map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());
    }

    public enum KeyDisposition {
        VERTICAL,
        HORIZONTAL
    }
}
