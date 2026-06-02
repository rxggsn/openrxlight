package cn.ggsn.openrxlight.lang;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public class Lists2 {

    public static <T> List<T> empty() {
        return Lists.newArrayList();
    }

    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        if (elements == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(elements);
    }

    @SuppressWarnings("null")
    public static <T> List<T> ofArray(T[] elmenet) {
        if (elmenet == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(Arrays.stream(elmenet).iterator());
    }

    public static <T, V> List<T> map(List<V> list, Function<V, T> function) {
        if (list == null) {
            return Lists2.empty();
        }
        List<T> result = Lists.newArrayList();
        for (V v : list) {
            result.add(function.apply(v));
        }
        return result;
    }

    public static <T, V> List<T> mapNotNull(List<V> list, Function<V, T> function) {
        if (list == null) {
            return Lists2.empty();
        }
        List<T> result = Lists.newArrayList();
        for (V v : list) {
            T apply = function.apply(v);
            if (apply != null) {
                result.add(apply);
            }
        }
        return result;
    }

    public static <T, V> List<T> indexMap(List<V> list, BiFunction<Integer, V, T> function) {
        if (list == null) {
            return Lists2.empty();
        }
        List<T> result = Lists.newArrayList();
        int idx = 0;
        for (V v : list) {
            result.add(function.apply(idx, v));
            idx++;
        }
        return result;
    }

    public static <T> List<T> filter(List<T> list, Function<T, Boolean> function) {
        List<T> result = Lists.newArrayList();
        if (isEmpty(list)) {
            return result;
        }
        for (T t : list) {
            if (function.apply(t)) {
                result.add(t);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> concat(List<T>... lists) {
        List<T> result = Lists.newArrayList();
        for (List<T> list : lists) {
            if (list != null) {
                result.addAll(list);
            }
        }
        return result;
    }

    public static <T> void foreach(List<T> list, Consumer<T> function) {
        if (isEmpty(list)) {
            return;
        }
        for (T t : list) {
            function.accept(t);
        }
    }

    public static <T> void indexForeach(List<T> list, BiConsumer<Integer, T> function) {
        if (isEmpty(list)) {
            return;
        }
        int idx = 0;
        for (T t : list) {
            function.accept(idx, t);
            idx++;
        }
    }

    public static <T> T reduce(List<T> list, Function<T, T> function) {
        T result = null;
        for (T t : list) {
            result = function.apply(t);
        }
        return result;
    }

    public static <T, U> U reduce(List<T> list, U init, BiFunction<U, T, U> function, BinaryOperator<U> combiner) {
        if (Lists2.isEmpty(list)) {
            return init;
        }

        U result = init;
        for (T t : list) {
            result = function.apply(result, t);
        }

        return combiner.apply(result, init);
    }

    public static <T, U> U reduce(List<T> list, U init, BiFunction<T, T, U> function) {
        if (Lists2.isEmpty(list)) {
            return init;
        }


        U result = init;
        T last = null;
        for (T t : list) {
            if (last == null) {
                last = t;
                continue;
            }
            result = function.apply(last, t);
        }

        return result;
    }

    public static <T> boolean isEmpty(List<T> objects) {
        return objects == null || objects.isEmpty();
    }

    public static <T> boolean isNotEmpty(List<T> objects) {
        return !isEmpty(objects);
    }

    public static <T> T get(List<T> objects, int index) {
        if (objects == null || objects.isEmpty()) {
            return null;
        }
        return objects.get(index);
    }

    public static <T> boolean contains(List<T> objects, Predicate<T> predicate) {
        if (objects == null || objects.isEmpty()) {
            return false;
        }
        for (T t : objects) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean matchAll(List<T> refundInfos, Predicate<T> predicate) {
        if (refundInfos == null || refundInfos.isEmpty()) {
            return false;
        }
        for (T t : refundInfos) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    public static <K, T> Map<K, T> mapSelf(List<T> list, Function<T, K> function) {
        Map<K, T> result = Maps2.empty();
        for (T t : list) {
            result.put(function.apply(t), t);
        }
        return result;
    }

    public static <T> void removeIf(List<T> items, Predicate<T> predicate) {
        if (items == null || items.isEmpty()) {
            return;
        }
        items.removeIf(predicate);
    }

    public static <K, T> Map<K, List<T>> group(List<T> list, Function<T, K> mapper) {
        Map<K, List<T>> result = Maps2.empty();
        if (isEmpty(list)) {
            return result;
        }
        for (T t : list) {
            K key = mapper.apply(t);
            List<T> value = result.computeIfAbsent(key, k -> Lists2.empty());
            value.add(t);
        }
        return result;
    }

    public static <T> T first(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public static <T> int size(List<T> list) {
        if (isEmpty(list)) {
            return 0;
        }

        return list.size();
    }

    public static <T> boolean anyOf(List<T> list, Predicate<T> predicate) {
        if (isEmpty(list)) {
            return false;
        }
        for (T t : list) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> List<T> append(List<T> list, T val) {
        if (isEmpty(list)) {
            return Lists2.of(val);
        }

        list.add(val);
        return list;
    }

    public static <T> String join(List<T> values, String separator, Function<T, String> mapper) {
        if (isEmpty(values)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(mapper.apply(values.get(i)));
        }
        return sb.toString();
    }

    public static <T, V> List<T> flatMap(List<V> list, Function<V, List<T>> function) {
        if (isEmpty(list)) {
            return Lists2.empty();
        }
        List<T> result = Lists.newArrayList();
        for (V v : list) {
            List<T> apply = function.apply(v);
            result.addAll(apply);
        }
        return result;
    }

    public static <T, V> List<T> flatMapNotNull(List<V> list, Function<V, List<T>> function) {
        if (isEmpty(list)) {
            return Lists2.empty();
        }
        List<T> result = Lists.newArrayList();
        for (V v : list) {
            List<T> apply = function.apply(v);
            if (apply != null) {
                result.addAll(apply);
            }
        }
        return result;
    }

    public static <T> T min(
            List<T> list, Comparator<T> comparator) {
        if (isEmpty(list)) {
            return null;
        }

        list.sort(comparator);
        return list.get(0);
    }

    public static <T> List<T> minOrEq(List<T> list, Comparator<T> comparator) {
        if (isEmpty(list)) {
            return Lists2.empty();
        }

        list.sort(comparator);
        T min = list.get(0);
        List<T> result = Lists2.empty();
        for (T t : list) {
            if (comparator.compare(t, min) == 0) {
                result.add(t);
            } else {
                break;
            }
        }
        return result;
    }

    public static <T> T last(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

}
