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

package obsidian.util.concurrent.atomic;

import obsidian.util.concurrent.Box;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe {@link Box} implementation backed by {@link AtomicReference}.
 *
 * <h2>Overview</h2>
 * {@code AtomicBox} is a concurrent {@link Box} whose value is stored inside an
 * {@link AtomicReference}, providing lock-free, thread-safe access and updates.
 *
 * <p>
 * This implementation supports atomic operations such as
 * {@link #compareAndSet(Object, Object)} and {@link #getAndSet(Object)},
 * making it suitable for highly concurrent scenarios.
 *
 * <h2>Memory semantics</h2>
 * All read and write operations have the same visibility and ordering guarantees
 * as {@link AtomicReference}:
 * <ul>
 *   <li>Reads see the most recent successful write</li>
 *   <li>Writes establish a <i>happens-before</i> relationship with subsequent reads</li>
 * </ul>
 *
 * <h2>Comparison with other Box implementations</h2>
 * <ul>
 *   <li>{@code AtomicBox}: lock-free, supports CAS, best for concurrency</li>
 *   <li>{@code VolatileBox}: volatile visibility only, no CAS</li>
 *   <li>{@code PlainBox}: no thread-safety guarantees</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Box<Integer> counter = new AtomicBox<>(0);
 *
 * counter.updateAndGet(v -> v + 1);
 *
 * boolean updated = counter.compareAndSet(1, 42);
 * }</pre>
 *
 * @param <T> the type of the boxed value
 *
 * @see Box
 * @see AtomicReference
 * @see AtomicVolatileBox
 * @see obsidian.util.concurrent.atomic.PlainBox
 */
public final class AtomicBox<T> implements Box<T> {

    private final AtomicReference<T> reference;

    /**
     * Creates a new {@code AtomicBox} with the given initial value.
     *
     * @param initial the initial value (may be {@code null})
     */
    public AtomicBox(T initial) {
        this.reference = new AtomicReference<>(initial);
    }

    /**
     * Returns the current value.
     *
     * @return the current value (may be {@code null})
     */
    @Override
    public T get() {
        return reference.get();
    }

    /**
     * Atomically sets the value to {@code value}.
     *
     * @param value the new value (may be {@code null})
     */
    @Override
    public void set(T value) {
        reference.set(value);
    }

    /**
     * Atomically sets the value to {@code value} and returns the previous value.
     *
     * @param value the new value
     * @return the previous value
     */
    @Override
    public T getAndSet(T value) {
        return reference.getAndSet(value);
    }

    /**
     * Atomically sets the value to {@code update} if the current value is equal
     * to {@code expect}.
     *
     * @param expect the expected current value
     * @param update the new value
     * @return {@code true} if successful, {@code false} otherwise
     */
    @Override
    public boolean compareAndSet(T expect, T update) {
        return reference.compareAndSet(expect, update);
    }
}
