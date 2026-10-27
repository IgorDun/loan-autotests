package dev.kozhevnikov.autotests.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.kozhevnikov.autotests.exception.AutotestException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Сохраняет данные как ключ-зачение для их дальнейшего использования
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoTestContext {
    private static final ThreadLocal<Map<String, Object>> data = ThreadLocal.withInitial(HashMap::new);

    /**
     * Проверяет наличие указанного ключа в текущих данных.
     * @param key ключ для проверки
     * @return {@code true}, если текущие данные содержат указанный ключ, если нет - {@code false}
     */
    public static boolean containsKey(String key) {
        return data.get().containsKey(key);
    }

    /**
     * Возвращает значение, связанное с указанным ключом.
     * @param key ключ, значение которого нужно получить
     * @return значение, связанное с указанным ключом или {@code null}, если ключ отсутствует
     */
    public static Object get(String key) {
        return data.get().get(key);
    }

    /**
     * Устанавливает указанное значение по указанному ключу в текущих данных.
     * @param key ключ, по которому следует сохранить значение
     * @param value значение, которое следует сохранить
     */
    public static void put(String key, Object value) {
        data.get().put(key, value);
    }

    /**
     * Возвращает значение, связанное с указанным ключом.
     * @param key ключ, значение которого нужно получить
     * @return значение, связанное с указанным ключом
     * @throws dev.kozhevnikov.autotests.exception.AutotestException если значение по указанному ключу отсутствует
     */
    public static Object getNotNull(String key) {
        String errorMessage = "В контексте отсутствует значение '%s'";
        return Optional.ofNullable(get(key))
                .orElseThrow(() -> new AutotestException(String.format(errorMessage, key)));
    }

    /**
     * Очищает текущий контекст данных, удаляя все значения.
     */
    public static void clearContext() {
        data.remove();
    }
}
