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
import java.util.NavigableSet;

/**
 * Persistent (immutable) sorted set abstraction.
 *
 * <h2>Overview</h2>
 * {@code OSortedSet} represents an immutable, ordered set whose elements are
 * kept in sorted order, either by their natural ordering or by a provided
 * {@link Comparator}.
 *
 * <p>
 * This interface combines:
 * <ul>
 *   <li>The immutability guarantees of {@link OSet}</li>
 *   <li>The ordering and navigation capabilities of {@link NavigableSet}</li>
 * </ul>
 *
 * <p>
 * All operations that would normally mutate a {@link NavigableSet} instead
 * return a <b>new</b> {@code OSortedSet} instance, preserving the original.
 *
 * <h2>Ordering semantics</h2>
 * <ul>
 *   <li>Elements are unique (set semantics)</li>
 *   <li>Ordering is defined by {@link #comparator()} or natural ordering</li>
 *   <li>Iteration respects sorted order</li>
 * </ul>
 *
 * <h2>Core operations</h2>
 * <ul>
 *   <li>{@link #plus(Object)} – inserts an element while preserving order</li>
 *   <li>{@link #minus(Object)} – removes an element</li>
 *   <li>{@link #plusAll(java.util.Collection)} – inserts multiple elements</li>
 *   <li>{@link #minusAll(java.util.Collection)} – removes multiple elements</li>
 * </ul>
 *
 * <h2>Navigation</h2>
 * Since this interface extends {@link NavigableSet}, it supports navigation
 * operations such as:
 * <ul>
 *   <li>{@code first()}, {@code last()}</li>
 *   <li>{@code lower()}, {@code floor()}, {@code ceiling()}, {@code higher()}</li>
 *   <li>{@code headSet()}, {@code tailSet()}, {@code subSet()}</li>
 * </ul>
 *
 * Implementations are expected to return immutable views or new persistent
 * instances for all such operations.
 *
 * <h2>Descending view</h2>
 * {@link #descendingSet()} returns a persistent, immutable view of this set
 * with reversed ordering.
 *
 * <h2>Immutability and deprecated mutators</h2>
 * All mutating methods inherited from {@link NavigableSet} or {@link java.util.Set}
 * are deprecated and should throw {@link UnsupportedOperationException}.
 *
 * <p>
 * Use {@code plus*} and {@code minus*} methods instead.
 *
 * @param <E> the element type
 *
 * @see OSet
 * @see NavigableSet
 * @see TreeOSet
 */
public interface OSortedSet<E> extends OSet<E>, NavigableSet<E> {

    /**
     * Returns a new sorted set with {@code e} inserted.
     *
     * <p>
     * If the element already exists, implementations typically return {@code this}.
     *
     * @param e the element to add
     * @return a new {@code OSortedSet} containing {@code e}, or {@code this} if unchanged
     */
    @Override
    OSortedSet<E> plus(E e);

    /**
     * Returns a new sorted set with all elements from {@code list} inserted.
     *
     * @param list elements to add
     * @return a new {@code OSortedSet} containing the added elements
     */
    @Override
    OSortedSet<E> plusAll(java.util.Collection<? extends E> list);

    /**
     * Returns a new sorted set with {@code e} removed.
     *
     * @param e the element to remove
     * @return a new {@code OSortedSet} without {@code e}, or {@code this} if unchanged
     */
    @Override
    OSortedSet<E> minus(Object e);

    /**
     * Returns a new sorted set with all elements in {@code list} removed.
     *
     * @param list elements to remove
     * @return a new {@code OSortedSet} without the specified elements
     */
    @Override
    OSortedSet<E> minusAll(java.util.Collection<?> list);

    /**
     * Returns the comparator used to order the elements in this set,
     * or {@code null} if natural ordering is used.
     *
     * @return the comparator, or {@code null} for natural ordering
     */
    @Override
    Comparator<? super E> comparator();

    /**
     * Returns a persistent sorted set with the reverse ordering of this set.
     *
     * <p>
     * The returned set is immutable and reflects the same elements as this set,
     * but iterates in descending order.
     *
     * @return a descending-order {@code OSortedSet}
     */
    @Override
    @NotNull
    OSortedSet<E> descendingSet();
}
