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

/**
 * Central factory for empty persistent collection instances.
 *
 * <h2>Overview</h2>
 * {@code Empty} provides canonical empty instances for all persistent collection
 * types in {@code obsidian.collections}.
 *
 * <p>
 * These empty values serve as the identity elements for their respective data
 * structures and are the recommended starting point for incremental construction
 * via {@code plus(...)} operations.
 *
 * <h2>Why this class exists</h2>
 * Instead of exposing constructors or relying on {@code null}, Obsidian collections
 * use explicit empty values:
 * <ul>
 *   <li>to make emptiness explicit</li>
 *   <li>to avoid accidental {@code null} usage</li>
 *   <li>to encourage functional, persistent-style construction</li>
 * </ul>
 *
 * <h2>Persistence guarantees</h2>
 * All empty instances returned by this class are:
 * <ul>
 *   <li>Immutable</li>
 *   <li>Thread-safe</li>
 *   <li>Safe to share across threads and pipelines</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * OVector<Integer> v = Empty.vector();
 * v = v.plus(1).plus(2).plus(3);
 *
 * PMap<String, Integer> m = Empty.map()
 *     .plus("a", 1)
 *     .plus("b", 2);
 * }</pre>
 *
 * <h2>Relationship with {@link P}</h2>
 * While {@link P} provides convenient bulk factories (e.g. {@code vectorOf(1,2,3)}),
 * {@code Empty} provides the foundational zero-values used internally by those helpers
 * and exposed for low-level or incremental usage.
 *
 * @see P
 * @see OStack
 * @see OQueue
 * @see OVector
 * @see OSet
 * @see OMap
 * @see OSortedSet
 * @see OSortedMap
 */
public final class Empty {

    private Empty() {
    }

    /**
     * Returns the empty persistent stack.
     */
    public static <E> OStack<E> stack() {
        return ConsOStack.empty();
    }

    /**
     * Returns the empty persistent queue.
     */
    public static <E> OQueue<E> queue() {
        return AmortizedOQueue.empty();
    }

    /**
     * Returns the empty persistent vector.
     */
    public static <E> OVector<E> vector() {
        return ChunkedOVector.empty();
    }

    /**
     * Returns the empty persistent set.
     */
    public static <E> OSet<E> set() {
        return HashTrieOSet.empty();
    }

    /**
     * Returns the empty persistent map.
     */
    public static <K, V> OMap<K, V> map() {
        return HashTriePMap.empty();
    }

    /**
     * Returns the empty persistent sorted map using natural key ordering.
     */
    @Contract(" -> new")
    public static <K extends Comparable<? super K>, V> @NotNull OSortedMap<K, V> sortedMap() {
        return TreeOMap.empty();
    }

    /**
     * Returns the empty persistent sorted map using the provided comparator.
     *
     * @param comparator the comparator used to order keys
     */
    @Contract("_ -> new")
    public static <K, V> @NotNull OSortedMap<K, V> sortedMap(java.util.Comparator<? super K> comparator) {
        return TreeOMap.empty(comparator);
    }

    /**
     * Returns the empty persistent sorted set using natural element ordering.
     */
    @Contract(" -> new")
    public static <E extends Comparable<? super E>> @NotNull OSortedSet<E> sortedSet() {
        return TreeOSet.empty();
    }

    /**
     * Returns the empty persistent sorted set using the provided comparator.
     *
     * @param comparator the comparator used to order elements
     */
    @Contract("_ -> new")
    public static <E> @NotNull OSortedSet<E> sortedSet(java.util.Comparator<? super E> comparator) {
        return TreeOSet.empty(comparator);
    }
}
