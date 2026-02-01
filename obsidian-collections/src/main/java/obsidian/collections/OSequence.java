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
import java.util.List;

/**
 * Persistent (immutable) indexed sequence abstraction.
 *
 * <h2>Overview</h2>
 * {@code OSequence} represents an immutable, list-like sequence of elements.
 * It combines the semantics of {@link java.util.List} with persistent data
 * structure behavior: all modifying operations return a <em>new</em> sequence
 * instead of mutating the current instance.
 *
 * <p>
 * This interface is the backbone of Obsidian’s persistent linear collections.
 * More specialized structures such as {@link OVector} and {@link OStack}
 * extend {@code OSequence} and refine performance characteristics or usage patterns.
 *
 * <h2>Immutability contract</h2>
 * Any method that would normally mutate a {@link List} instead returns a new
 * {@code OSequence}:
 * <ul>
 *   <li>{@link #plus(Object)}</li>
 *   <li>{@link #with(int, Object)}</li>
 *   <li>{@link #plus(int, Object)}</li>
 *   <li>{@link #minus(Object)}</li>
 *   <li>{@link #minus(int)}</li>
 * </ul>
 *
 * The original instance is never modified.
 *
 * <h2>Indexed operations</h2>
 * {@code OSequence} supports random access and positional updates:
 * <ul>
 *   <li>Replace elements via {@link #with(int, Object)}</li>
 *   <li>Insert elements via {@link #plus(int, Object)}</li>
 *   <li>Remove elements via {@link #minus(int)}</li>
 *   <li>Extract slices via {@link #subList(int, int)}</li>
 * </ul>
 *
 * <h2>Bulk operations</h2>
 * Bulk methods operate in iteration order and return new sequences:
 * <ul>
 *   <li>{@link #plusAll(Collection)}</li>
 *   <li>{@link #plusAll(int, Collection)}</li>
 *   <li>{@link #minusAll(Collection)}</li>
 * </ul>
 *
 * <h2>Compatibility with {@link List}</h2>
 * {@code OSequence} extends {@link List} for interoperability and familiarity.
 * However, all mutating {@link List} methods are marked as {@link Deprecated}
 * and must not be used. Implementations are expected to throw
 * {@link UnsupportedOperationException} for these methods.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * OSequence<Integer> seq = Empty.vector();
 * seq = seq.plus(1).plus(2).plus(3);
 *
 * OSequence<Integer> updated = seq.with(1, 42); // [1, 42, 3]
 * OSequence<Integer> removed = seq.minus(0);    // [2, 3]
 * }</pre>
 *
 * @param <E> the element type
 *
 * @see OCollection
 * @see OVector
 * @see OStack
 */
public interface OSequence<E> extends OCollection<E>, List<E> {

    /**
     * Returns a new sequence with {@code e} appended.
     */
    @Override
    OSequence<E> plus(E e);

    /**
     * Returns a new sequence with all elements from {@code list} appended.
     */
    @Override
    OSequence<E> plusAll(Collection<? extends E> list);

    /**
     * Returns a new sequence where the element at {@code index} is replaced by {@code value}.
     *
     * @param index the index to replace (0-based)
     * @param value the new element
     * @return a new sequence with the element replaced
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    OSequence<E> with(int index, E value);

    /**
     * Returns a new sequence with {@code value} inserted at {@code index}.
     *
     * @param index the insertion index (0-based)
     * @param value the element to insert
     * @return a new sequence with the element inserted
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    OSequence<E> plus(int index, E value);

    /**
     * Returns a new sequence with all elements from {@code list} inserted
     * starting at {@code index}.
     *
     * @param index the insertion index (0-based)
     * @param list elements to insert
     * @return a new sequence with the inserted elements
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    OSequence<E> plusAll(int index, Collection<? extends E> list);

    /**
     * Returns a new sequence with the first occurrence of {@code e} removed.
     */
    @Override
    OSequence<E> minus(Object e);

    /**
     * Returns a new sequence with all elements contained in {@code list} removed.
     */
    @Override
    OSequence<E> minusAll(Collection<?> list);

    /**
     * Returns a new sequence with the element at {@code index} removed.
     *
     * @param index the index to remove (0-based)
     * @return a new sequence without the element
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    OSequence<E> minus(int index);

    /**
     * Returns a new sequence representing the range {@code [fromIndex, toIndex)}.
     */
    @Override
    OSequence<E> subList(int fromIndex, int toIndex);

    @Deprecated
    @Override
    boolean addAll(int index, Collection<? extends E> c);

    @Deprecated
    @Override
    E set(int index, E element);

    @Deprecated
    @Override
    void add(int index, E element);

    @Deprecated
    @Override
    E remove(int index);
}
