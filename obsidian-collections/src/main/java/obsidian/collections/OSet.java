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
import java.util.Set;

/**
 * Persistent (immutable) set abstraction.
 *
 * <h2>Overview</h2>
 * {@code OSet} represents an immutable mathematical set: a collection of
 * <b>unique</b> elements with no defined iteration order (unless specified
 * by a concrete implementation).
 *
 * <p>
 * This interface combines the semantics of {@link Set} with the immutability
 * guarantees of {@link OCollection}. Any operation that would normally mutate
 * a {@link Set} instead returns a <b>new</b> {@code OSet} instance.
 *
 * <h2>Core operations</h2>
 * <ul>
 *   <li>{@link #plus(Object)} – adds an element (if absent)</li>
 *   <li>{@link #minus(Object)} – removes an element (if present)</li>
 *   <li>{@link #plusAll(Collection)} – adds multiple elements</li>
 *   <li>{@link #minusAll(Collection)} – removes multiple elements</li>
 * </ul>
 *
 * <h2>Set semantics</h2>
 * <ul>
 *   <li>Duplicate insertions have no effect</li>
 *   <li>Element equality is determined by {@link Object#equals(Object)}</li>
 *   <li>Hash-based or tree-based behavior depends on the implementation</li>
 * </ul>
 *
 * <h2>Immutability and deprecated mutators</h2>
 * Methods inherited from {@link Set} and {@link Collection} that imply
 * in-place mutation are deprecated and expected to throw
 * {@link UnsupportedOperationException}.
 *
 * <p>
 * Use the persistent alternatives ({@code plus*}, {@code minus*}) instead.
 *
 * <h2>Creation</h2>
 * An empty persistent set can be created via:
 * <pre>{@code
 * OSet<String> set = OSet.empty();
 * }</pre>
 *
 * @param <E> the element type
 *
 * @see OCollection
 * @see Set
 * @see HashTrieOSet
 * @see TreeOSet
 */
public interface OSet<E> extends OCollection<E>, Set<E> {

    /**
     * Returns a new set with {@code e} added.
     *
     * <p>
     * If the element already exists, implementations typically return {@code this}.
     *
     * @param e the element to add
     * @return a new {@code OSet} containing {@code e}, or {@code this} if unchanged
     */
    @Override
    OSet<E> plus(E e);

    /**
     * Returns a new set with all elements from {@code list} added.
     *
     * @param list elements to add
     * @return a new {@code OSet} containing the added elements
     */
    @Override
    OSet<E> plusAll(Collection<? extends E> list);

    /**
     * Returns a new set with {@code e} removed.
     *
     * <p>
     * If the element is not present, implementations typically return {@code this}.
     *
     * @param e the element to remove
     * @return a new {@code OSet} without {@code e}, or {@code this} if unchanged
     */
    @Override
    OSet<E> minus(Object e);

    /**
     * Returns a new set with all elements contained in {@code list} removed.
     *
     * @param list elements to remove
     * @return a new {@code OSet} without the specified elements
     */
    @Override
    OSet<E> minusAll(Collection<?> list);

    /**
     * Returns an empty persistent set.
     *
     * @param <E> the element type
     * @return an empty {@code OSet}
     */
    static <E> OSet<E> empty() {
        return Empty.set();
    }
}
