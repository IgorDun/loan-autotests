package dev.kozhevnikov.autotests.utils.startdata.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class StartDataContextUtil {
    private static final Map<String, Object> data = new HashMap<>();

    public static String get(String key) {
        return (String) data.get(key);
    }

    public static void put(String key, Object value) {
        data.put(key, value);
    }

    public static Boolean getBool(String key) {
        return Boolean.parseBoolean(get(key, String.class));
    }

    public static UUID getUUID(String key) {
        return get(key, UUID.class);
    }

    public static Integer getInt(String key) {
        return get(key, Integer.class);
    }

    public static <T> T get(String key, Class<T> tClass) {
        return tClass.cast(data.get(key));
    }

    @SuppressWarnings("all")
    public static <S> List<S> getList(String key, Class<S> genericTypeClass) {
        List castedObject = List.class.cast(data.get(key));
        return cast(castedObject, genericTypeClass);
    }

    public static List<String> getList(String key) {
        return getList(key, String.class);
    }

    @SuppressWarnings("all")
    public static <S1, S2> Map<S1, S2> getMap(String key, Class<S1> keyTypeClass1, Class<S2> valueTypeClass2) {
        Map castedObject = Map.class.cast(data.get(key));
        return cast(castedObject, keyTypeClass1, valueTypeClass2);
    }

    public static Map<String, String> getMap(String key) {
        return getMap(key, String.class, String.class);
    }

    private static <T> List<T> cast(List<Object> list, Class<T> tClass) {
        return (Objects.isNull(list))
                ? null
                : list.stream()
                .map(tClass::cast)
                .collect(Collectors.toList());
    }

    private static <T1, T2> Map<T1, T2> cast(Map<String, Object> map, Class<T1> t1Class, Class<T2> t2Class) {
        return (Objects.isNull(map))
                ? null
                : map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        it -> t1Class.cast(it.getKey()),
                        it -> t2Class.cast(it.getValue())
                        )
                );
    }

    public static void clearContext() {
        data.clear();
    }
}
