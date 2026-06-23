package cn.ggsn.openrxlight.lang;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sets2 {

    public static <T, V> Set<T> map(Set<V> set, Function<V, T> function) {
        if (set == null) {
            return Sets.newHashSet();
        }
        Set<T> result = Sets.newHashSet();
        for (V v : set) {
            if (v == null) {
                continue;
            }
            result.add(function.apply(v));
        }
        return result;
    }

    public static <T, V> Set<T> mapNotNull(Set<V> set, Function<V, T> function) {
        if (set == null) {
            return Sets.newHashSet();
        }
        Set<T> result = Sets.newHashSet();
        for (V v : set) {
            if (v == null) {
                continue;
            }
            T apply = function.apply(v);
            if (apply != null) {
                result.add(apply);
            }
        }
        return result;
    }

    public static <V> void foreach(Set<V> set, Consumer<V> consumer) {
        if (set == null) {
            return;
        }
        for (V v : set) {
            if (v == null) {
                continue;
            }
            consumer.accept(v);
        }
    }


}
