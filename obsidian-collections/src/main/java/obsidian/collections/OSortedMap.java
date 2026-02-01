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

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.NavigableMap;

/**
 * Persistent (immutable) {@link NavigableMap} with sorted keys.
 *
 * <h2>Overview</h2>
 * {@code OSortedMap} is an immutable, persistent map whose keys are kept in a
 * total order (either natural ordering or a provided {@link Comparator}).
 *
 * <p>
 * It combines:
 * <ul>
 *   <li>The persistent mutation model from {@link OMap} (operations return new instances)</li>
 *   <li>The navigation and range APIs from {@link NavigableMap}</li>
 * </ul>
 *
 * <h2>Immutability</h2>
 * All "mutation" operations return a new {@code OSortedMap} and never modify the current
 * instance:
 * <ul>
 *   <li>{@link #plus(Object, Object)} – associates a key/value</li>
 *   <li>{@link #plusAll(java.util.Map)} – associates all entries</li>
 *   <li>{@link #minus(Object)} – removes a key</li>
 *   <li>{@link #minusAll(java.util.Collection)} – removes multiple keys</li>
 * </ul>
 *
 * The mutating methods inherited from {@link NavigableMap}/{@link java.util.Map} are
 * marked {@link Deprecated} and are expected to throw {@link UnsupportedOperationException}
 * in implementations.
 *
 * <h2>Ordering</h2>
 * Key ordering is defined by {@link #comparator()}:
 * <ul>
 *   <li>If {@code comparator() == null}, natural ordering is used (keys must be {@link Comparable}).</li>
 *   <li>If non-null, the comparator defines the order and range semantics.</li>
 * </ul>
 *
 * <h2>Views</h2>
 * Navigation views should also be persistent / immutable-friendly:
 * <ul>
 *   <li>{@link #descendingMap()} returns a reverse-order view of this map</li>
 *   <li>{@link #navigableKeySet()} returns an {@link OSortedSet} view of the keys</li>
 *   <li>{@link #descendingKeySet()} returns the keys in descending order</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * OSortedMap<Integer, String> m = Empty.sortedMap();
 * m = m.plus(2, "B").plus(1, "A").plus(3, "C");
 *
 * // Range navigation
 * OSortedMap<Integer, String> head = (OSortedMap<Integer, String>) m.headMap(3, false);
 *
 * // Immutable key views
 * OSortedSet<Integer> keysAsc  = m.navigableKeySet();    // [1, 2, 3]
 * OSortedSet<Integer> keysDesc = m.descendingKeySet();   // [3, 2, 1]
 * }</pre>
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @see OMap
 * @see NavigableMap
 * @see OSortedSet
 * @see Empty#sortedMap()
 */
public interface OSortedMap<K, V> extends OMap<K, V>, NavigableMap<K, V> {

    /**
     * Returns a new map with {@code key} associated to {@code value}.
     * If the key already exists, its value is replaced in the returned map.
     *
     * @param key the key to associate
     * @param value the value to store (may be null depending on implementation rules)
     * @return a new {@code OSortedMap} containing the association
     */
    @Override
    OSortedMap<K, V> plus(K key, V value);

    /**
     * Returns a new map containing all entries from {@code map} added to this map.
     * Existing keys are replaced by the values from {@code map}.
     *
     * @param map entries to add
     * @return a new {@code OSortedMap} containing all entries
     */
    @Override
    OSortedMap<K, V> plusAll(java.util.Map<? extends K, ? extends V> map);

    /**
     * Returns a new map with {@code key} removed.
     * If the key does not exist, returns {@code this}.
     *
     * @param key the key to remove
     * @return a new {@code OSortedMap} without the key, or {@code this} if absent
     */
    @Override
    OSortedMap<K, V> minus(Object key);

    /**
     * Returns a new map with all keys in {@code keys} removed.
     *
     * @param keys keys to remove
     * @return a new {@code OSortedMap} without those keys
     */
    @Override
    OSortedMap<K, V> minusAll(java.util.Collection<?> keys);

    /**
     * Returns a descending-order view of this map.
     *
     * <p>
     * The returned map preserves navigable semantics (range queries, first/last, etc.)
     * in reverse key order.
     *
     * @return a descending-order {@code OSortedMap}
     */
    @Override
    OSortedMap<K, V> descendingMap();

    /**
     * Returns an immutable navigable set view of the keys in ascending order.
     *
     * @return an {@link OSortedSet} view of keys
     */
    @Override
    OSortedSet<K> navigableKeySet();

    /**
     * Returns an immutable navigable set view of the keys in descending order.
     *
     * @return an {@link OSortedSet} view of keys in reverse order
     */
    @Override
    OSortedSet<K> descendingKeySet();

    /**
     * Returns the comparator used to order keys, or {@code null} if natural ordering is used.
     *
     * @return the comparator defining key ordering, or {@code null}
     */
    @Override
    Comparator<? super K> comparator();

    /**
     * @deprecated {@code OSortedMap} is immutable; use {@link #plus(Object, Object)} instead.
     */
    @Deprecated
    @Override
    V put(K key, V value);

    /**
     * @deprecated {@code OSortedMap} is immutable; use {@link #minus(Object)} instead.
     */
    @Deprecated
    @Override
    V remove(Object key);

    /**
     * @deprecated {@code OSortedMap} is immutable; use {@link #plusAll(java.util.Map)} instead.
     */
    @Deprecated
    @Override
    void putAll(java.util.@NotNull Map<? extends K, ? extends V> m);

    /**
     * @deprecated {@code OSortedMap} is immutable; use {@link Empty#sortedMap()} to create an empty map.
     */
    @Deprecated
    @Override
    void clear();
}
