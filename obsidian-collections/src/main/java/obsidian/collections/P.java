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

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Factory utilities for creating persistent (immutable) collection instances.
 *
 * <h2>Overview</h2>
 * {@code P} is a convenience entry point for constructing the collection types provided
 * by {@code obsidian.collections}.
 *
 * <p>
 * It provides small, allocation-friendly helpers similar in spirit to:
 * <ul>
 *   <li>{@link java.util.List#of(Object[])} / {@link java.util.Set#of(Object[])}</li>
 *   <li>{@code copyOf(...)} constructors</li>
 * </ul>
 * but targeting Obsidian persistent collections such as {@link OStack}, {@link OQueue},
 * {@link OVector}, {@link OSet}, {@link OMap}, {@link OSortedSet}, and {@link OSortedMap}.
 *
 * <h2>Null policy</h2>
 * Unless stated otherwise, these factories do <b>not</b> accept {@code null} elements or keys.
 * If a {@code null} is encountered, a {@link NullPointerException} is thrown with a helpful message.
 *
 * <h2>Performance notes</h2>
 * These builders create collections by repeatedly applying {@code plus(...)} starting from
 * the corresponding {@link Empty} instance. This keeps the API simple and predictable and
 * preserves the semantic order expected by each structure (e.g., stack LIFO construction).
 *
 * <h2>Map pair builders</h2>
 * The varargs map builders follow a pair-based convention:
 * <pre>{@code
 * P.mapOf(k1, v1, k2, v2, k3, v3, ...);
 * }</pre>
 * The {@code rest} array must have even length; otherwise an {@link IllegalArgumentException} is thrown.
 *
 * <h2>Duplicate key merge helpers</h2>
 * This class also provides {@link BinaryOperator} helpers that can be used when building maps
 * from streams (e.g., {@code Collectors.toMap}) to handle duplicate keys:
 * <ul>
 *   <li>{@link #failOnDuplicateKeys()}</li>
 *   <li>{@link #keepFirst()}</li>
 *   <li>{@link #keepLast()}</li>
 * </ul>
 *
 * @see Empty
 */
@SuppressWarnings("unused")
public final class P {

    private P() {
    }

    /**
     * Creates a stack containing the given items.
     *
     * <p>
     * The resulting stack will have {@code items[0]} as the top element (typical "of" semantics).
     *
     * @throws NullPointerException if {@code items} is null or any element is null.
     */
    @SafeVarargs
    public static <E> OStack<E> stackOf(E... items) {
        requireNonNull(items, "items");
        OStack<E> s = Empty.stack();
        for (int i = items.length - 1; i >= 0; i--) {
            s = s.plus(requireNonNull(items[i], "null element"));
        }
        return s;
    }

    /**
     * Creates a stack from a collection.
     *
     * @throws NullPointerException if {@code c} is null or contains null elements.
     */
    public static <E> OStack<E> stackCopyOf(Collection<? extends E> c) {
        requireNonNull(c, "collection");
        Object[] arr = c.toArray();
        OStack<E> s = Empty.stack();
        for (int i = arr.length - 1; i >= 0; i--) {
            @SuppressWarnings("unchecked") E e = (E) arr[i];
            s = s.plus(requireNonNull(e, "null element"));
        }
        return s;
    }

    /**
     * Creates a queue containing the given items in encounter order.
     *
     * @throws NullPointerException if {@code items} is null or any element is null.
     */
    @SafeVarargs
    public static <E> OQueue<E> queueOf(E... items) {
        requireNonNull(items, "items");
        OQueue<E> q = Empty.queue();
        for (E e : items) q = q.plus(requireNonNull(e, "null element"));
        return q;
    }

    /**
     * Creates a persistent queue containing the elements of the given collection in encounter order.
     *
     * @param <E> the type of elements in the queue
     * @param c the collection whose elements are to be placed into the queue
     * @return a new persistent queue containing the elements of the specified collection
     * @throws NullPointerException if {@code c} is null or contains null elements
     */
    public static <E> OQueue<E> queueCopyOf(Collection<? extends E> c) {
        requireNonNull(c, "collection");
        OQueue<E> q = Empty.queue();
        for (E e : c) q = q.plus(requireNonNull(e, "null element"));
        return q;
    }

    /**
     * Creates a vector containing the given items in encounter order.
     *
     * @throws NullPointerException if {@code items} is null or any element is null.
     */
    @SafeVarargs
    public static <E> OVector<E> vectorOf(E... items) {
        requireNonNull(items, "items");
        OVector<E> v = Empty.vector();
        for (E e : items) v = (OVector<E>) v.plus(requireNonNull(e, "null element"));
        return v;
    }

    /**
     * Creates a persistent vector containing the elements from the given collection in encounter order.
     *
     * @param <E> the type of elements in the vector
     * @param c the collection whose elements are to be placed into the vector
     * @return a new persistent vector containing the elements of the specified collection
     * @throws NullPointerException if {@code c} is null or contains null elements
     */
    public static <E> OVector<E> vectorCopyOf(Collection<? extends E> c) {
        requireNonNull(c, "collection");
        OVector<E> v = Empty.vector();
        for (E e : c) v = (OVector<E>) v.plus(requireNonNull(e, "null element"));
        return v;
    }

    /**
     * Creates a set containing the given items.
     *
     * @throws NullPointerException if {@code items} is null or any element is null.
     */
    @SafeVarargs
    public static <E> OSet<E> setOf(E... items) {
        requireNonNull(items, "items");
        OSet<E> s = Empty.set();
        for (E e : items) s = s.plus(requireNonNull(e, "null element"));
        return s;
    }

    /**
     * Creates a persistent set containing the elements of the specified collection.
     *
     * @param <E> the type of elements in the set
     * @param c the collection whose elements are to be placed into the set
     * @return a new persistent set containing the elements of the specified collection
     * @throws NullPointerException if {@code c} is null or contains null elements
     */
    public static <E> OSet<E> setCopyOf(Collection<? extends E> c) {
        requireNonNull(c, "collection");
        OSet<E> s = Empty.set();
        for (E e : c) s = s.plus(requireNonNull(e, "null element"));
        return s;
    }

    /**
     * Returns an empty persistent map.
     */
    public static <K, V> OMap<K, V> mapOf() {
        return Empty.map();
    }

    /**
     * Creates a map with a single entry.
     *
     * @throws NullPointerException if {@code k1} is null.
     */
    public static <K, V> OMap<K, V> mapOf(K k1, V v1) {
        return Empty.<K, V>map().plus(requireNonNull(k1, "null key"), v1);
    }

    /**
     * Creates a map from pairs: {@code mapOf(k1,v1, k2,v2, ...)}.
     *
     * <p>
     * The {@code rest} array must contain an even number of elements and be structured as:
     * {@code [k2, v2, k3, v3, ...]}.
     *
     * @throws NullPointerException if {@code k1} is null, {@code rest} is null, or any key is null.
     * @throws IllegalArgumentException if {@code rest.length} is odd.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> OMap<K, V> mapOf(K k1, V v1, Object... rest) {
        requireNonNull(k1, "null key");
        requireNonNull(rest, "rest");
        if ((rest.length % 2) != 0) {
            throw new IllegalArgumentException("rest must have even length: [k2,v2,k3,v3,...]");
        }
        OMap<K, V> m = Empty.<K, V>map().plus(k1, v1);
        for (int i = 0; i < rest.length; i += 2) {
            K k = (K) rest[i];
            V v = (V) rest[i + 1];
            m = m.plus(requireNonNull(k, "null key"), v);
        }
        return m;
    }

    /**
     * Creates a persistent map from a regular {@link Map}.
     *
     * @throws NullPointerException if {@code src} is null or contains null keys.
     */
    public static <K, V> OMap<K, V> mapCopyOf(Map<? extends K, ? extends V> src) {
        requireNonNull(src, "map");
        OMap<K, V> out = Empty.map();
        for (Map.Entry<? extends K, ? extends V> e : src.entrySet()) {
            out = out.plus(requireNonNull(e.getKey(), "null key"), e.getValue());
        }
        return out;
    }

    /**
     * Creates a sorted set using natural ordering.
     *
     * @throws NullPointerException if {@code items} is null or any element is null.
     */
    @SafeVarargs
    public static <E extends Comparable<? super E>> OSortedSet<E> sortedSetOf(E... items) {
        requireNonNull(items, "items");
        OSortedSet<E> s = Empty.sortedSet();
        for (E e : items) s = s.plus(requireNonNull(e, "null element"));
        return s;
    }

    /**
     * Creates a sorted set using the provided comparator.
     *
     * @throws NullPointerException if {@code comparator} or {@code items} is null, or any element is null.
     */
    @SafeVarargs
    public static <E> OSortedSet<E> sortedSetOf(Comparator<? super E> comparator, E... items) {
        requireNonNull(comparator, "comparator");
        requireNonNull(items, "items");
        OSortedSet<E> s = Empty.sortedSet(comparator);
        for (E e : items) s = s.plus(requireNonNull(e, "null element"));
        return s;
    }


    public static <E extends Comparable<? super E>> OSortedSet<E> sortedSetCopyOf(Collection<? extends E> c) {
        requireNonNull(c, "collection");
        OSortedSet<E> s = Empty.sortedSet();
        for (E e : c) s = s.plus(requireNonNull(e, "null element"));
        return s;
    }

    public static <E> OSortedSet<E> sortedSetCopyOf(Comparator<? super E> comparator, Collection<? extends E> c) {
        requireNonNull(comparator, "comparator");
        requireNonNull(c, "collection");
        OSortedSet<E> s = Empty.sortedSet(comparator);
        for (E e : c) s = s.plus(requireNonNull(e, "null element"));
        return s;
    }

    /**
     * Creates a sorted set using the provided comparator.
     *
     * @throws NullPointerException if {@code comparator} or {@code items} is null, or any element is null.
     */
    @Contract(" -> new")
    public static <K extends Comparable<? super K>, V> @NotNull OSortedMap<K, V> sortedMapOf() {
        return Empty.sortedMap();
    }

    /**
     * Returns an empty sorted map using the provided comparator.
     *
     * @throws NullPointerException if {@code comparator} is null.
     */
    @Contract("_ -> new")
    public static <K, V> @NotNull OSortedMap<K, V> sortedMapOf(Comparator<? super K> comparator) {
        return Empty.sortedMap(comparator);
    }

    public static <K extends Comparable<? super K>, V> OSortedMap<K, V> sortedMapCopyOf(Map<? extends K, ? extends V> src) {
        requireNonNull(src, "map");
        OSortedMap<K, V> out = Empty.sortedMap();
        for (Map.Entry<? extends K, ? extends V> e : src.entrySet()) {
            out = out.plus(requireNonNull(e.getKey(), "null key"), e.getValue());
        }
        return out;
    }

    public static <K, V> OSortedMap<K, V> sortedMapCopyOf(Comparator<? super K> comparator, Map<? extends K, ? extends V> src) {
        requireNonNull(comparator, "comparator");
        requireNonNull(src, "map");
        OSortedMap<K, V> out = Empty.sortedMap(comparator);
        for (Map.Entry<? extends K, ? extends V> e : src.entrySet()) {
            out = out.plus(requireNonNull(e.getKey(), "null key"), e.getValue());
        }
        return out;
    }

    /**
     * Creates a sorted map from pairs: {@code sortedMapOf(k1,v1, k2,v2, ...)} using natural ordering.
     *
     * @throws NullPointerException if {@code k1} is null, {@code rest} is null, or any key is null.
     * @throws IllegalArgumentException if {@code rest.length} is odd.
     */
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>, V> OSortedMap<K, V> sortedMapOf(K k1, V v1, Object... rest) {
        requireNonNull(k1, "null key");
        requireNonNull(rest, "rest");
        if ((rest.length % 2) != 0) {
            throw new IllegalArgumentException("rest must have even length: [k2,v2,k3,v3,...]");
        }
        OSortedMap<K, V> m = Empty.<K, V>sortedMap().plus(k1, v1);
        int i = 0;
        while (i < rest.length) {
            K k = (K) rest[i];
            V v = (V) rest[i + 1];
            m = m.plus(requireNonNull(k, "null key"), v);
            i += 2;
        }
        return m;
    }

    /**
     * Returns a merge function that throws when a duplicate key is encountered.
     *
     * <p>
     * Useful with {@link java.util.stream.Collectors#toMap(Function, Function, BinaryOperator)}.
     */
    @Contract(pure = true)
    public static <V> @NotNull BinaryOperator<V> failOnDuplicateKeys() {
        return (oldV, newV) -> {
            throw new IllegalStateException("duplicate key");
        };
    }

    /**
     * Returns a merge function that keeps the first value for a duplicate key.
     */
    @Contract(pure = true)
    public static <V> @NotNull BinaryOperator<V> keepFirst() {
        return (oldV, newV) -> oldV;
    }

    /**
     * Returns a merge function that keeps the last value for a duplicate key.
     */
    @Contract(pure = true)
    public static <V> @NotNull BinaryOperator<V> keepLast() {
        return (oldV, newV) -> newV;
    }
}
