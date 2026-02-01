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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Persistent (immutable) map abstraction.
 *
 * <h2>Overview</h2>
 * {@code OMap} represents an immutable key-value map.
 * Instead of mutating state, all modification operations return
 * a new {@code OMap} instance, preserving the original map unchanged.
 *
 * <p>
 * This design enables:
 * <ul>
 *   <li>Structural sharing for memory efficiency</li>
 *   <li>Safe usage in concurrent and functional-style code</li>
 *   <li>Predictable behavior without side effects</li>
 * </ul>
 *
 * <h2>Mutation semantics</h2>
 * The following operations return new map instances:
 * <ul>
 *   <li>{@link #plus(Object, Object)} – adds or replaces a key-value pair</li>
 *   <li>{@link #plusAll(Map)} – adds all entries from another map</li>
 *   <li>{@link #minus(Object)} – removes a key</li>
 *   <li>{@link #minusAll(Collection)} – removes multiple keys</li>
 * </ul>
 *
 * <p>
 * Standard mutating {@link Map} methods such as {@link #put(Object, Object)},
 * {@link #remove(Object)} and {@link #clear()} are explicitly deprecated and
 * must not be used.
 *
 * <h2>Optional access</h2>
 * {@link #getOpt(Object)} provides a null-safe way to retrieve values using
 * {@link Optional}.
 *
 * <h2>Empty instance</h2>
 * {@link #empty()} returns the canonical empty persistent map implementation
 * provided by {@link Empty#map()}.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @see Empty#map()
 * @see HashTriePMap
 */
public interface OMap<K, V> extends Map<K, V> {

    /**
     * Returns a new map containing all entries of this map plus the given key-value pair.
     * If the key already exists, its value is replaced.
     *
     * @param key   the key to add or update
     * @param value the value associated with the key
     * @return a new {@code OMap} instance with the updated entry
     */
    OMap<K, V> plus(K key, V value);

    /**
     * Returns a new map containing all entries of this map plus all entries
     * from the provided map.
     *
     * @param map the map whose entries are to be added
     * @return a new {@code OMap} instance containing all entries
     */
    OMap<K, V> plusAll(Map<? extends K, ? extends V> map);

    /**
     * Returns a new map with the specified key removed.
     *
     * @param key the key to remove
     * @return a new {@code OMap} instance without the specified key
     */
    OMap<K, V> minus(Object key);

    /**
     * Returns a new map with all specified keys removed.
     *
     * @param keys the collection of keys to remove
     * @return a new {@code OMap} instance without the specified keys
     */
    OMap<K, V> minusAll(Collection<?> keys);

    /**
     * Returns the value associated with the given key wrapped in an {@link Optional}.
     *
     * @param key the key whose associated value is to be returned
     * @return an {@link Optional} containing the value if present, or empty otherwise
     */
    default Optional<V> getOpt(K key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * @deprecated Persistent maps are immutable.
     * Use {@link #plus(Object, Object)} instead.
     */
    @Deprecated
    @Override
    V put(K key, V value);

    /**
     * @deprecated Persistent maps are immutable.
     * Use {@link #minus(Object)} instead.
     */
    @Deprecated
    @Override
    V remove(Object key);

    /**
     * @deprecated Persistent maps are immutable.
     * Use {@link #plusAll(Map)} instead.
     */
    @Deprecated
    @Override
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * @deprecated Persistent maps are immutable.
     * This operation is not supported.
     */
    @Deprecated
    @Override
    void clear();

    /**
     * Returns the canonical empty persistent map instance.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty {@code OMap}
     */
    static <K, V> OMap<K, V> empty() {
        return Empty.map();
    }
}
