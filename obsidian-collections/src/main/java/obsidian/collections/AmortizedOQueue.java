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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Persistent (immutable) queue implementation with amortized {@code O(1)} operations.
 *
 * <h2>Overview</h2>
 * {@code AmortizedOQueue} is an immutable queue backed by two persistent stacks:
 * <ul>
 *   <li><b>front</b> — elements available for dequeue / peek</li>
 *   <li><b>back</b> — elements recently enqueued</li>
 * </ul>
 *
 * <p>
 * This is a classic functional queue design (often called a "two-stack queue"),
 * where:
 * <ul>
 *   <li>{@code plus(e)} pushes onto {@code back}</li>
 *   <li>{@code minus()} removes from {@code front}</li>
 *   <li>when {@code front} becomes empty, {@code back} is reversed into {@code front}</li>
 * </ul>
 *
 * <h2>Complexity</h2>
 * Most operations are amortized constant time:
 * <ul>
 *   <li>{@link #plus(Object)} — amortized {@code O(1)}</li>
 *   <li>{@link #minus()} — amortized {@code O(1)}</li>
 *   <li>{@link #peek()} — amortized {@code O(1)}</li>
 * </ul>
 *
 * <p>
 * The "expensive" step is normalization (moving elements from {@code back} to {@code front}),
 * which costs {@code O(n)} but happens only when {@code front} is empty, yielding amortized
 * {@code O(1)} over a sequence of operations.
 *
 * <h2>Immutability</h2>
 * This queue is persistent:
 * <ul>
 *   <li>All mutating operations return a <b>new</b> queue instance.</li>
 *   <li>Existing instances are never modified.</li>
 *   <li>Instances are safe to share across threads.</li>
 * </ul>
 *
 * <h2>Null policy</h2>
 * {@code null} elements are not supported. Attempts to insert {@code null} will throw
 * {@link NullPointerException}.
 *
 * <h2>Interoperability with {@link Queue}</h2>
 * This class extends {@link AbstractCollection} and implements {@link OQueue}, but it is not
 * a mutable {@link Queue}. Standard mutating methods like {@link #offer(Object)} and
 * {@link #poll()} are marked {@link Deprecated} and throw {@link UnsupportedOperationException}.
 *
 * <h2>Iteration order</h2>
 * The iterator yields elements in FIFO order:
 * <ol>
 *   <li>all items from {@code front} (already in correct order)</li>
 *   <li>then items from {@code back} in reverse order</li>
 * </ol>
 *
 * @param <E> the element type
 *
 * @see OQueue
 * @see OStack
 * @see ConsOStack
 */
public final class AmortizedOQueue<E> extends AbstractCollection<E> implements OQueue<E> {

    private static final AmortizedOQueue<?> EMPTY = new AmortizedOQueue<>(ConsOStack.empty(), ConsOStack.empty(), 0);

    private final OStack<E> front;
    private final OStack<E> back;
    private final int       size;

    private AmortizedOQueue(OStack<E> front, OStack<E> back, int size) {
        this.front = front;
        this.back  = back;
        this.size  = size;
    }

    /**
     * Returns the canonical empty queue instance.
     *
     * <p>
     * The returned instance is shared and allocation-free.
     */
    @SuppressWarnings("unchecked")
    public static <E> AmortizedOQueue<E> empty() {
        return (AmortizedOQueue<E>) EMPTY;
    }

    /**
     * Returns the number of elements currently stored in this queue.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns an iterator over the elements in FIFO order.
     *
     * <p>
     * The iterator is backed by an immutable snapshot of the queue contents.
     */
    @Override
    public @NotNull Iterator<E> iterator() {
        List<E> out = new ArrayList<>(size);
        out.addAll(front);
        ArrayList<E> tmp = new ArrayList<>(back);
        Collections.reverse(tmp);
        out.addAll(tmp);
        return Collections.unmodifiableList(out).iterator();
    }

    /**
     * Returns the front element of the queue without removing it.
     *
     * @return the first element, or {@code null} if the queue is empty.
     */
    @Contract(pure = true)
    @Override
    public @Nullable E peek() {
        if (size == 0) return null;
        AmortizedOQueue<E> q = normalized();
        return q.front.getFirst();
    }

    /**
     * Returns a new queue with {@code e} appended at the end (enqueue).
     *
     * <p>
     * This operation is amortized {@code O(1)}.
     *
     * @param e the element to append (must not be {@code null})
     * @return a new queue containing the additional element
     * @throws NullPointerException if {@code e} is {@code null}
     */
    @Override
    public OQueue<E> plus(E e) {
        Objects.requireNonNull(e, "null elements are not supported");
        return new AmortizedOQueue<>(front, back.plus(e), size + 1).normalized();
    }

    /**
     * Returns a new queue containing all elements of {@code list} appended in encounter order.
     *
     * @param list the elements to append
     * @return a new queue containing the appended elements
     * @throws NullPointerException if {@code list} is null or contains null elements
     */
    @Override
    public OQueue<E> plusAll(Collection<? extends E> list) {
        Objects.requireNonNull(list, "list");
        OQueue<E> q = this;
        for (E e : list) q = q.plus(e);
        return q;
    }

    /**
     * Returns a new queue with the front element removed (dequeue).
     *
     * <p>
     * If this queue is empty, returns {@code this}.
     *
     * <p>
     * This operation is amortized {@code O(1)}.
     *
     * @return a new queue without the front element
     */
    @Override
    public OQueue<E> minus() {
        if (size == 0) return this;
        AmortizedOQueue<E> q = normalized();
        OStack<E> newFront = q.front.minus(0);
        return new AmortizedOQueue<>(newFront, q.back, size - 1).normalized();
    }

    /**
     * Returns a new queue with the first occurrence of {@code e} removed.
     *
     * <p>
     * This is a linear operation ({@code O(n)}) and rebuilds the queue in FIFO order.
     *
     * <p>
     * If {@code e} is not present, returns {@code this}.
     *
     * @param e the element to remove (may be {@code null})
     * @return a new queue without the first occurrence of {@code e}
     */
    @Override
    public OQueue<E> minus(Object e) {
        if (size == 0) return this;
        ArrayList<E> all = new ArrayList<>(size);
        boolean removed = false;
        for (E x : this) {
            if (!removed && Objects.equals(x, e)) {
                removed = true;
                continue;
            }
            all.add(x);
        }
        if (!removed) return this;

        OQueue<E> q = empty();
        for (E x : all) q = q.plus(x);
        return q;
    }

    /**
     * Returns a new queue containing only elements not present in {@code list}.
     *
     * <p>
     * This is a linear operation ({@code O(n)}) and rebuilds the queue.
     *
     * @param list the elements to remove
     * @return a new queue with all matching elements removed
     * @throws NullPointerException if {@code list} is null
     */
    @Override
    public OQueue<E> minusAll(Collection<?> list) {
        Objects.requireNonNull(list, "list");
        if (list.isEmpty() || size == 0) return this;

        OQueue<E> q = empty();
        for (E x : this) if (!list.contains(x)) q = q.plus(x);
        return q;
    }

    /**
     * Ensures the queue invariant:
     * <ul>
     *   <li>If {@code size == 0} → queue is {@link #empty()}</li>
     *   <li>If {@code front} is not empty → queue is already usable</li>
     *   <li>If {@code front} is empty → move elements from {@code back} into {@code front}</li>
     * </ul>
     *
     * <p>
     * This method performs the amortization step by rebuilding {@code front} from {@code back}.
     */
    private AmortizedOQueue<E> normalized() {
        if (size == 0) return empty();
        if (!front.isEmpty()) return this;

        ArrayList<E> tmp = new ArrayList<>(back);
        OStack<E> nf = ConsOStack.empty();
        for (E e : tmp) nf = nf.plus(e);
        return new AmortizedOQueue<>(nf, ConsOStack.empty(), size);
    }

    @Deprecated
    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public E poll() {
        throw new UnsupportedOperationException("immutable");
    }

    @Contract(pure = true)
    @Deprecated
    @Override
    public @Nullable E element() {
        return null;
    }

    @Deprecated
    @Override
    public E remove() {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException("immutable");
    }
}
