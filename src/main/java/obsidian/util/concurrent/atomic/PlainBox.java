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
 * Non-thread-safe {@link Box} implementation with plain field semantics.
 *
 * <h2>Overview</h2>
 * {@code PlainBox} is the simplest {@link Box} implementation.
 * It stores the value in a regular field, with no synchronization,
 * no volatile semantics, and no atomic guarantees.
 *
 * <p>
 * This implementation is intended for:
 * <ul>
 *   <li>Single-threaded code</li>
 *   <li>Thread-confined usage</li>
 *   <li>Performance-critical paths where synchronization is unnecessary</li>
 * </ul>
 *
 * <h2>Thread-safety</h2>
 * {@code PlainBox} provides <strong>no</strong> thread-safety guarantees:
 * <ul>
 *   <li>No visibility guarantees between threads</li>
 *   <li>No atomicity for read/write operations</li>
 *   <li>Data races may occur if accessed concurrently</li>
 * </ul>
 *
 * If the box is accessed from multiple threads, use:
 * <ul>
 *   <li>{@code AtomicBox} for lock-free, CAS-based concurrency</li>
 *   <li>{@code AtomicVolatileBox} for visibility-only guarantees</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Box<String> box = new PlainBox<>("hello");
 *
 * box.set("world");
 * String v = box.get();
 * }</pre>
 *
 * @param <T> the type of the boxed value
 *
 * @see Box
 * @see obsidian.util.concurrent.atomic.AtomicBox
 * @see obsidian.util.concurrent.atomic.AtomicVolatileBox
 */
public final class PlainBox<T> implements Box<T> {

    private T value;

    /**
     * Creates a new {@code PlainBox} with the given initial value.
     *
     * @param initial the initial value (may be {@code null})
     */
    public PlainBox(T initial) {
        this.value = initial;
    }

    /**
     * Returns the current value.
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
     * <p>No synchronization or visibility guarantees are provided.
     *
     * @param value the new value (may be {@code null})
     */
    @Override
    public void set(T value) {
        this.value = value;
    }
}
