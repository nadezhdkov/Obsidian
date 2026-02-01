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
 * Persistent (immutable) stack / list-like sequence abstraction.
 *
 * <h2>Overview</h2>
 * {@code OStack} represents an immutable sequence optimized for stack-style usage
 * (LIFO: last-in, first-out), while still supporting indexed operations like a list.
 *
 * <p>
 * In the Obsidian collections module, {@code OStack} is commonly used as the
 * foundational persistent "linear" structure: it behaves like a stack for
 * push/pop patterns, but it can also be treated as an indexed sequence via
 * {@link OSequence} operations.
 *
 * <h2>Immutability</h2>
 * {@code OStack} is <b>persistent</b> and <b>immutable</b>:
 * methods that would normally modify the structure return a <em>new</em>
 * {@code OStack} instance and keep the original unchanged.
 *
 * <h2>Core semantics</h2>
 * Typical stack semantics:
 * <ul>
 *   <li>{@link #plus(Object)} – pushes an element (returns a new stack)</li>
 *   <li>{@link #minus(int)} – removes an element by index (0-based)</li>
 * </ul>
 *
 * <p>
 * The exact "top" side depends on the implementation contract of your concrete stack.
 * In most persistent stacks, index {@code 0} is treated as the top/head.
 *
 * <h2>Indexed operations</h2>
 * Because {@code OStack} extends {@link OSequence}, it supports list-like operations:
 * <ul>
 *   <li>{@link #with(int, Object)} – replaces an element at an index</li>
 *   <li>{@link #plus(int, Object)} – inserts at an index</li>
 *   <li>{@link #minus(int)} – removes at an index</li>
 *   <li>{@link #subList(int, int)} – extracts a range</li>
 * </ul>
 *
 * <h2>Bulk operations</h2>
 * Bulk operations preserve order (based on {@code list} iteration order)
 * and return new persistent stacks:
 * <ul>
 *   <li>{@link #plusAll(Collection)}</li>
 *   <li>{@link #plusAll(int, Collection)}</li>
 *   <li>{@link #minusAll(Collection)}</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * OStack<String> s = Empty.stack();
 * s = s.plus("A").plus("B").plus("C"); // push
 *
 * String top = s.get(0);               // commonly top/head at index 0
 * OStack<String> popped = s.minus(0);  // remove top
 * }</pre>
 *
 * @param <E> the element type
 *
 * @see OSequence
 * @see OCollection
 * @see ConsOStack
 */
public interface OStack<E> extends OSequence<E> {

    /**
     * Returns a new stack with {@code e} pushed/added.
     *
     * @param e the element to add
     * @return a new {@code OStack} with the element added
     */
    @Override
    OStack<E> plus(E e);

    /**
     * Returns a new stack with all elements from {@code list} added.
     *
     * @param list elements to add
     * @return a new {@code OStack} containing the added elements
     */
    @Override
    OStack<E> plusAll(Collection<? extends E> list);

    /**
     * Returns a new stack where the element at index {@code i} is replaced with {@code e}.
     *
     * @param i the index to replace (0-based)
     * @param e the new element value
     * @return a new {@code OStack} with the element replaced
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    @Override
    OStack<E> with(int i, E e);

    /**
     * Returns a new stack with {@code e} inserted at index {@code i}.
     *
     * @param i the insertion index (0-based)
     * @param e the element to insert
     * @return a new {@code OStack} with the element inserted
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    @Override
    OStack<E> plus(int i, E e);

    /**
     * Returns a new stack with all elements from {@code list} inserted starting at index {@code i}.
     *
     * @param i the insertion index (0-based)
     * @param list elements to insert
     * @return a new {@code OStack} with the inserted elements
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    @Override
    OStack<E> plusAll(int i, Collection<? extends E> list);

    /**
     * Returns a new stack with the first occurrence of {@code e} removed.
     *
     * @param e the element to remove
     * @return a new {@code OStack} without the element, or {@code this} if not present
     */
    @Override
    OStack<E> minus(Object e);

    /**
     * Returns a new stack with all elements contained in {@code list} removed.
     *
     * @param list elements to remove
     * @return a new {@code OStack} without the specified elements
     */
    @Override
    OStack<E> minusAll(Collection<?> list);

    /**
     * Returns a new stack with the element at index {@code i} removed.
     *
     * @param i the index to remove (0-based)
     * @return a new {@code OStack} without the element at {@code i}
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    @Override
    OStack<E> minus(int i);

    /**
     * Returns a new stack containing elements in the range {@code [start, end)}.
     *
     * @param start start index (inclusive)
     * @param end end index (exclusive)
     * @return a new {@code OStack} representing the requested slice
     * @throws IndexOutOfBoundsException if the range is invalid
     */
    @Override
    OStack<E> subList(int start, int end);

    /**
     * Returns a new stack containing elements from {@code start} (inclusive) to the end.
     *
     * @param start start index (inclusive)
     * @return a new {@code OStack} representing the tail slice
     * @throws IndexOutOfBoundsException if {@code start} is out of range
     */
    OStack<E> subList(int start);
}
