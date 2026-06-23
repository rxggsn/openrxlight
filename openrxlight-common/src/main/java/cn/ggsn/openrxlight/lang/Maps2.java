package cn.ggsn.openrxlight.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import java.util.Map.Entry;

public class Maps2 {

    public static <K, V> Map<K, V> of(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    public static <K, V> Map<K, V> empty() {
        return new HashMap<>();
    }

    public static <K, V> boolean isEmpty(Map<K, V> objectMap) {
        return Objects.isNull(objectMap) || objectMap.isEmpty();
    }

    public static <K, V> Map<K, V> asImmutableMap(@NonNull List<Entry<K, V>> entries) {
        return ImmutableMap.copyOf(entries);
    }

    public static <K, V> Map<K, V> merge(Map<K, V> m1, Map<K, V> m2) {
        Map<K, V> result = empty();
        if (m1 != null)
            result.putAll(m1);
        if (m2 != null)
            result.putAll(m2);
        return result;
    }

    public static <K, V, R> Map<K, R> mapValue(@NonNull Map<K, V> map, Function<V, R> mapper) {
        Map<K, R> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return result;
    }

    public static <K, V, R> Map<R, V> mapKey(@NonNull Map<K, V> map, Function<K, R> mapper) {
        Map<R, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(mapper.apply(entry.getKey()), entry.getValue());
        }
        return result;
    }

}
