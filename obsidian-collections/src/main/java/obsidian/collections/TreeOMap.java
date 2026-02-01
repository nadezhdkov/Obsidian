/*
 * Copyright 2026 Rick M. Viana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package obsidian.collections;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

public final class TreeOMap<K, V> extends AbstractMap<K, V> implements OSortedMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NavigableMap<K, V>    map;
    private final Comparator<? super K> comparator;

    private transient Set<Entry<K, V>>  entrySetCache;

    private TreeOMap(NavigableMap<K, V> map, Comparator<? super K> comparator) {
        this.map        = requireNonNull(map, "map");
        this.comparator = requireNonNull(comparator, "comparator");
    }

    @Contract(" -> new")
    public static <K extends Comparable<? super K>, V> @NotNull TreeOMap<K, V> empty() {
        return empty(Comparator.naturalOrder());
    }

    @Contract("_ -> new")
    public static <K, V> @NotNull TreeOMap<K, V> empty(Comparator<? super K> comparator) {
        requireNonNull(comparator, "comparator");
        return new TreeOMap<>(Collections.unmodifiableNavigableMap(new TreeMap<>(comparator)), comparator);
    }

    @Contract("_ -> new")
    public static <K extends Comparable<? super K>, V> @NotNull TreeOMap<K, V> from(Map<? extends K, ? extends V> map) {
        return from(Comparator.naturalOrder(), map);
    }

    @Contract("_, _ -> new")
    public static <K, V> @NotNull TreeOMap<K, V> from(Comparator<? super K> comparator, Map<? extends K, ? extends V> map) {
        requireNonNull(comparator, "comparator");
        requireNonNull(map, "map");
        TreeMap<K, V> tm = new TreeMap<>(comparator);
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            K k = requireNonNull(e.getKey(), "map contains null key");
            tm.put(k, e.getValue());
        }
        return new TreeOMap<>(Collections.unmodifiableNavigableMap(tm), comparator);
    }

    public static <K extends Comparable<? super K>, V> TreeOMap<K, V> singleton(K key, V value) {
        return singleton(Comparator.naturalOrder(), key, value);
    }

    public static <K, V> TreeOMap<K, V> singleton(Comparator<? super K> comparator, K key, V value) {
        return TreeOMap.<K, V>empty(comparator).plus(requireNonNull(key, "key"), value);
    }

    public static <T, K extends Comparable<? super K>, V> @NotNull Collector<T, ?, TreeOMap<K, V>> toTreePMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return toTreePMap(Comparator.naturalOrder(), keyMapper, valueMapper,
                (oldV, newV) -> {
                    throw new IllegalStateException("duplicate key");
                });
    }

    public static <T, K, V> @NotNull Collector<T, ?, TreeOMap<K, V>> toTreePMap(
            Comparator<? super K>            comparator,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return toTreePMap(comparator, keyMapper, valueMapper,
                (oldV, newV) -> {
                    throw new IllegalStateException("duplicate key");
                });
    }

    public static <T, K, V> @NotNull Collector<T, ?, TreeOMap<K, V>> toTreePMap(
            Comparator<? super K>            comparator,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper,
            BinaryOperator<V>                mergeFunction
    ) {
        requireNonNull(comparator, "comparator");
        requireNonNull(keyMapper, "keyMapper");
        requireNonNull(valueMapper, "valueMapper");
        requireNonNull(mergeFunction, "mergeFunction");

        Supplier<TreeMap<K, V>> supplier = () -> new TreeMap<>(comparator);

        BiConsumer<TreeMap<K, V>, T> accumulator = (tm, t) -> {
            K k = requireNonNull(keyMapper.apply(t), "key is null");
            V v = valueMapper.apply(t);
            if (tm.containsKey(k)) {
                tm.put(k, mergeFunction.apply(tm.get(k), v));
            } else {
                tm.put(k, v);
            }
        };

        BinaryOperator<TreeMap<K, V>> combiner = (a, b) -> {
            for (Entry<K, V> e : b.entrySet()) {
                K k = e.getKey();
                V v = e.getValue();
                if (a.containsKey(k)) {
                    a.put(k, mergeFunction.apply(a.get(k), v));
                } else {
                    a.put(k, v);
                }
            }
            return a;
        };

        Function<TreeMap<K, V>, TreeOMap<K, V>> finisher = tm ->
                new TreeOMap<>(Collections.unmodifiableNavigableMap(tm), comparator);

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public TreeOMap<K, V> plus(K key, V value) {
        requireNonNull(key, "key is null");
        if (Objects.equals(map.get(key), value) && map.containsKey(key)) return this;

        TreeMap<K, V> tm = new TreeMap<>(comparator);
        tm.putAll(map);
        tm.put(key, value);
        return new TreeOMap<>(Collections.unmodifiableNavigableMap(tm), comparator);
    }

    @Override
    public TreeOMap<K, V> plusAll(Map<? extends K, ? extends V> m) {
        requireNonNull(m, "map is null");
        if (m.isEmpty()) return this;

        TreeMap<K, V> tm = new TreeMap<>(comparator);
        tm.putAll(map);
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            tm.put(requireNonNull(e.getKey(), "map contains null key"), e.getValue());
        }
        return new TreeOMap<>(Collections.unmodifiableNavigableMap(tm), comparator);
    }

    @Override
    public TreeOMap<K, V> minus(Object key) {
        requireNonNull(key, "key is null");
        if (!map.containsKey(key)) return this;

        TreeMap<K, V> tm = new TreeMap<>(comparator);
        tm.putAll(map);
        tm.remove(key);
        return new TreeOMap<>(Collections.unmodifiableNavigableMap(tm), comparator);
    }

    @Override
    public TreeOMap<K, V> minusAll(Collection<?> keys) {
        requireNonNull(keys, "keys is null");
        if (keys.isEmpty()) return this;

        TreeMap<K, V> tm = new TreeMap<>(comparator);
        tm.putAll(map);
        boolean changed = false;
        for (Object k : keys) {
            if (tm.containsKey(k)) {
                tm.remove(k);
                changed = true;
            }
        }
        return changed ? new TreeOMap<>(Collections.unmodifiableNavigableMap(tm), comparator) : this;
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        if (entrySetCache == null) {
            entrySetCache = Collections.unmodifiableSet(map.entrySet());
        }
        return entrySetCache;
    }

    @Override
    public K firstKey() {
        return map.firstKey();
    }

    @Override
    public K lastKey() {
        return map.lastKey();
    }

    @Override
    public Entry<K, V> firstEntry() {
        return map.firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return map.lastEntry();
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return map.lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return map.lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return map.floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return map.floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return map.ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return map.ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return map.higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return map.higherKey(key);
    }

    @Override
    public @NotNull OSortedMap<K, V> descendingMap() {
        NavigableMap<K, V> dm = map.descendingMap();
        return new TreeOMap<>(Collections.unmodifiableNavigableMap(new TreeMap<>(dm)), comparator.reversed());
    }

    @Contract(" -> new")
    @Override
    public @NotNull OSortedSet<K> navigableKeySet() {
        return new TreeOSet<>(map.navigableKeySet(), comparator);
    }

    @Contract(" -> new")
    @Override
    public @NotNull OSortedSet<K> descendingKeySet() {
        return new TreeOSet<>(map.descendingKeySet(), comparator.reversed());
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return map.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return map.headMap(toKey, inclusive);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return map.tailMap(fromKey, inclusive);
    }

    @Deprecated
    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return null;
    }

    @Deprecated
    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return null;
    }

    @Deprecated
    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return null;
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException("immutable");
    }
}