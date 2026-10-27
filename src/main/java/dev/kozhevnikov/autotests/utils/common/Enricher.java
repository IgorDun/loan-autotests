package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Обогащает данными различные объекты (строки, Map и т.д.)
 */
@Component
@Lazy
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Enricher {

    private static final String CONTEXT_EVAL_PATTERN_REGEXP = "\\$\\{([\\w\\.]+)(: ?([^}]+))?\\}";
    private static final Pattern CONTEXT_EVAL_PATTERN = Pattern.compile(CONTEXT_EVAL_PATTERN_REGEXP);

    private static final String EVAL_PATTERN_REGEXP = "@\\{([^}]+)}";
    private static final Pattern EVAL_PATTERN = Pattern.compile(EVAL_PATTERN_REGEXP);

    private final Evaluator evaluator;

    /**
     * Обогащает строку с использованием Map с параметрами.
     *
     * @param inputString входная строка, которую нужно обогатить
     * @param params      Map с параметрами
     * @return обогащенная строка
     */
    public String enrich(String inputString, Map<String, String> params) {
        if (params != null) {
            Map<String, String> enrichedParams = new HashMap<>();
            for (String key : params.keySet()) {
                enrichedParams.put(key, enrich(params.get(key)));
            }
            params = enrichedParams;
        }
        return enrich(EVAL_PATTERN, enrich(CONTEXT_EVAL_PATTERN, inputString, params), null);
    }

    /**
     * Обогащает строку.
     *
     * @param value строка, которую нужно обогатить
     * @return обогащенная строка
     */
    public String enrich(String value) {
        return enrich(value, null);
    }

    /**
     * Обогащает Map состоящую из строк.
     *
     * @param values Map строк, которую нужно обогатить
     * @return обогащенная Map
     */
    public Map<String, String> enrich(Map<String, String> values) {
        return values.entrySet()
                .stream()
                .map(entry -> new SimpleEntry<>(enrich(entry.getKey()), enrich(entry.getValue())))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    /**
     * Обогащает список строк.
     *
     * @param values список строк, который нужно обогатить
     * @return обогащенный список строк
     */
    public List<String> enrich(List<String> values) {
        return values.stream()
                .map(this::enrich)
                .collect(Collectors.toList());
    }

    private String enrich(Pattern pattern, String inputString, Map<String, String> params) {
        if (inputString != null && !inputString.isEmpty() && pattern.matcher(inputString).find()) {
            inputString = pattern.matcher(inputString)
                    .results()
                    .map(MatchResult::group)
                    .map(getFunctionForPattern(pattern, params))
                    .reduce(
                            inputString,
                            (str, entry) -> str.replace(entry.getKey(), entry.getValue()),
                            (str, str2) -> str2
                    );
        }
        return inputString;
    }

    private Function<String, Map.Entry<String, String>> getFunctionForPattern(Pattern pattern, Map<String, String> params) {
        if (pattern.equals(EVAL_PATTERN)) {
            return getEvalPatternFunction();
        } else {
            return getContextPatterFunction(params);
        }
    }

    private Function<String, Map.Entry<String, String>> getEvalPatternFunction() {
        return group -> new SimpleEntry<>(
                group,
                evaluator.evaluate(group.replaceAll(EVAL_PATTERN_REGEXP, "$1"), Object.class).toString()
        );
    }

    private Function<String, Map.Entry<String, String>> getContextPatterFunction(Map<String, String> params) {
        return group -> {
            String key = group.replaceAll(CONTEXT_EVAL_PATTERN_REGEXP, "$1");
            String enrichedGroup = Optional.ofNullable(params)
                    .map(parameters -> parameters.get(key))
                    .orElseGet(() -> Optional.ofNullable(AutoTestContext.get(key))
                            .map(Object::toString)
                            .orElseGet(() -> {
                                String alternateValue = group.replaceAll(CONTEXT_EVAL_PATTERN_REGEXP, "$3");
                                return alternateValue.isEmpty() ? alternateValue : evaluator.evaluate(alternateValue, Object.class).toString();
                            }));
            return new SimpleEntry<>(group, enrichedGroup);
        };
    }
}
