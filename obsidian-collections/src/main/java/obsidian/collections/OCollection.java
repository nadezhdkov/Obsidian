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

/**
 * Persistent (immutable) collection abstraction.
 *
 * <h2>Overview</h2>
 * {@code OCollection} defines the base contract for all immutable / persistent
 * collection types in the Obsidian collections module.
 *
 * <p>
 * Unlike standard {@link Collection} implementations, instances of
 * {@code OCollection} are <b>immutable</b>: operations that would normally
 * mutate the collection instead return a <em>new</em> collection instance.
 *
 * <h2>Mutation semantics</h2>
 * All "modifying" operations follow functional semantics:
 * <ul>
 *   <li>{@link #plus(Object)} – returns a new collection with an added element</li>
 *   <li>{@link #plusAll(Collection)} – returns a new collection with added elements</li>
 *   <li>{@link #minus(Object)} – returns a new collection with an element removed</li>
 *   <li>{@link #minusAll(Collection)} – returns a new collection with elements removed</li>
 * </ul>
 *
 * <p>
 * The original collection is never modified.
 *
 * <h2>Immutability guarantees</h2>
 * All mutating methods inherited from {@link Collection}
 * (such as {@link #add(Object)} or {@link #clear()})
 * are explicitly deprecated and must not be used.
 *
 * <p>
 * Implementations may throw {@link UnsupportedOperationException} if these methods
 * are called.
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li>Functional-style APIs</li>
 *   <li>Structural sharing for performance</li>
 *   <li>Thread-safety by immutability</li>
 * </ul>
 *
 * @param <E> the element type
 *
 * @see OSequence
 * @see OSet
 * @see OQueue
 * @see OStack
 */
public interface OCollection<E> extends Collection<E> {

    /**
     * Returns a new collection containing all elements of this collection
     * plus the given element.
     *
     * @param e the element to add
     * @return a new {@code OCollection} containing the added element
     */
    OCollection<E> plus(E e);

    /**
     * Returns a new collection containing all elements of this collection
     * plus all elements from the given collection.
     *
     * @param list the collection of elements to add
     * @return a new {@code OCollection} containing the added elements
     */
    OCollection<E> plusAll(Collection<? extends E> list);

    /**
     * Returns a new collection with the first occurrence of the given element removed.
     *
     * @param e the element to remove
     * @return a new {@code OCollection} without the specified element
     */
    OCollection<E> minus(Object e);

    /**
     * Returns a new collection with all elements contained in the given collection removed.
     *
     * @param list the collection of elements to remove
     * @return a new {@code OCollection} without the specified elements
     */
    OCollection<E> minusAll(Collection<?> list);

    /**
     * @deprecated Persistent collections are immutable.
     * Use {@link #plus(Object)} instead.
     */
    @Deprecated
    @Override
    boolean add(E e);

    /**
     * @deprecated Persistent collections are immutable.
     * Use {@link #minus(Object)} instead.
     */
    @Deprecated
    @Override
    boolean remove(Object o);

    /**
     * @deprecated Persistent collections are immutable.
     * Use {@link #plusAll(Collection)} instead.
     */
    @Deprecated
    @Override
    boolean addAll(Collection<? extends E> c);

    /**
     * @deprecated Persistent collections are immutable.
     * Use {@link #minusAll(Collection)} instead.
     */
    @Deprecated
    @Override
    boolean removeAll(Collection<?> c);

    /**
     * @deprecated Persistent collections are immutable.
     * This operation is not supported.
     */
    @Deprecated
    @Override
    boolean retainAll(Collection<?> c);

    /**
     * @deprecated Persistent collections are immutable.
     * This operation is not supported.
     */
    @Deprecated
    @Override
    void clear();
}