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
import java.util.Queue;

/**
 * Persistent (immutable) queue abstraction.
 *
 * <h2>Overview</h2>
 * {@code OQueue} represents an immutable FIFO queue (first-in, first-out).
 * It extends both {@link OCollection} (persistent semantics) and {@link Queue}
 * (Java interoperability), but does <b>not</b> support in-place mutation.
 *
 * <p>
 * All operations that would normally modify a {@link Queue} instead return a new
 * {@code OQueue} instance, keeping the original queue unchanged.
 *
 * <h2>Core operations</h2>
 * <ul>
 *   <li>{@link #plus(Object)} – enqueues an element (returns a new queue)</li>
 *   <li>{@link #minus()} – dequeues/removes the head element (returns a new queue)</li>
 *   <li>{@link #peek()} – inspects the head element without removing it</li>
 * </ul>
 *
 * <h2>Removal semantics</h2>
 * This interface provides two kinds of removals:
 * <ul>
 *   <li>{@link #minus()} removes the <b>head</b> element (FIFO dequeue)</li>
 *   <li>{@link #minus(Object)} removes the <b>first matching</b> element (implementation-defined but
 *       typically the first occurrence)</li>
 * </ul>
 *
 * <h2>Immutability and deprecated {@link Queue} mutators</h2>
 * Standard mutating queue methods are deprecated:
 * <ul>
 *   <li>{@link #offer(Object)}</li>
 *   <li>{@link #poll()}</li>
 *   <li>{@link #remove()}</li>
 * </ul>
 *
 * <p>
 * Implementations are expected to throw {@link UnsupportedOperationException} for these methods.
 *
 * @param <E> the element type
 *
 * @see OCollection
 * @see Queue
 * @see AmortizedOQueue
 */
public interface OQueue<E> extends OCollection<E>, Queue<E> {

    /**
     * Returns a new queue with the head element removed.
     *
     * <p>
     * If the queue is empty, implementations typically return {@code this}.
     *
     * @return a new {@code OQueue} without its head element
     */
    OQueue<E> minus();

    /**
     * Enqueues {@code e} and returns the updated queue.
     *
     * @param e the element to add
     * @return a new {@code OQueue} containing the added element
     */
    @Override
    OQueue<E> plus(E e);

    /**
     * Enqueues all elements from {@code list} (in iteration order) and returns the updated queue.
     *
     * @param list elements to add
     * @return a new {@code OQueue} containing the added elements
     */
    @Override
    OQueue<E> plusAll(Collection<? extends E> list);

    /**
     * Returns a new queue with the first occurrence of {@code e} removed.
     *
     * @param e element to remove
     * @return a new {@code OQueue} without the element, or {@code this} if not present
     */
    @Override
    OQueue<E> minus(Object e);

    /**
     * Returns a new queue with all elements that are contained in {@code list} removed.
     *
     * @param list elements to remove
     * @return a new {@code OQueue} without the specified elements
     */
    @Override
    OQueue<E> minusAll(Collection<?> list);

    /**
     * @deprecated Persistent queues are immutable.
     * Use {@link #plus(Object)} instead.
     */
    @Deprecated
    @Override
    boolean offer(E e);

    /**
     * @deprecated Persistent queues are immutable.
     * Use {@link #minus()} instead.
     */
    @Deprecated
    @Override
    E poll();

    /**
     * @deprecated Persistent queues are immutable.
     * Use {@link #minus()} instead.
     */
    @Deprecated
    @Override
    E remove();
}
