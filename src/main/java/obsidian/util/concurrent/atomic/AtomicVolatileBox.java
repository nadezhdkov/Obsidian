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

/**
 * {@link Box} implementation with {@code volatile} visibility semantics.
 *
 * <h2>Overview</h2>
 * {@code AtomicVolatileBox} stores its value in a {@code volatile} field,
 * guaranteeing <strong>visibility</strong> of writes across threads, but
 * providing <strong>no atomic read-modify-write operations</strong>.
 *
 * <p>
 * This implementation sits between {@link PlainBox} and {@link AtomicBox}
 * in terms of concurrency guarantees.
 *
 * <h2>Thread-safety guarantees</h2>
 * <ul>
 *   <li>✔ Writes by one thread are immediately visible to other threads</li>
 *   <li>✔ Reads always see the most recently written value</li>
 *   <li>✘ No atomicity for compound operations</li>
 *   <li>✘ No compare-and-set (CAS) support</li>
 * </ul>
 *
 * <h2>When to use</h2>
 * {@code AtomicVolatileBox} is ideal when:
 * <ul>
 *   <li>The value is replaced as a whole</li>
 *   <li>No read-modify-write logic is required</li>
 *   <li>You only need visibility guarantees</li>
 * </ul>
 *
 * <p>
 * Typical examples include configuration flags, state snapshots,
 * or periodically refreshed references.
 *
 * <h2>When <em>not</em> to use</h2>
 * <ul>
 *   <li>If updates depend on the previous value</li>
 *   <li>If multiple threads update concurrently</li>
 *   <li>If you need CAS or lock-free algorithms</li>
 * </ul>
 *
 * In those cases, prefer {@link AtomicBox}.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Box<Boolean> enabled = new AtomicVolatileBox<>(false);
 *
 * // writer thread
 * enabled.set(true);
 *
 * // reader thread
 * if (enabled.get()) {
 *     // visible immediately
 * }
 * }</pre>
 *
 * @param <T> the type of the boxed value
 *
 * @see Box
 * @see PlainBox
 * @see AtomicBox
 */
public final class AtomicVolatileBox<T> implements Box<T> {

    private volatile T value;

    /**
     * Creates a new {@code AtomicVolatileBox} with the given initial value.
     *
     * @param initial the initial value (may be {@code null})
     */
    public AtomicVolatileBox(T initial) {
        this.value = initial;
    }

    /**
     * Returns the current value.
     *
     * <p>
     * Due to {@code volatile} semantics, the returned value is guaranteed
     * to reflect the most recent write performed by any thread.
     *
     * @return the current value (may be {@code null})
     */
    @Override
    public T get() {
        return value;
    }

    /**
     * Sets the current value.
     *
     * <p>
     * The write is immediately visible to all threads that subsequently
     * read the value.
     *
     * @param value the new value (may be {@code null})
     */
    @Override
    public void set(T value) {
        this.value = value;
    }
}
