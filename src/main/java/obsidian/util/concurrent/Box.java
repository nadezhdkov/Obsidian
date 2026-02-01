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

package obsidian.util.concurrent;

import obsidian.util.concurrent.atomic.AtomicBox;
import obsidian.util.concurrent.atomic.PlainBox;
import obsidian.util.concurrent.atomic.AtomicVolatileBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

/**
 * A minimal mutable container ("box") for holding a single value.
 *
 * <h2>Overview</h2>
 * {@code Box} provides a small abstraction over "a reference to a value" with a consistent API.
 * It can be used as:
 * <ul>
 *   <li>a mutable holder in lambdas (workaround for effectively-final variables)</li>
 *   <li>a shared state container (depending on implementation)</li>
 *   <li>a primitive building block for concurrency utilities</li>
 * </ul>
 *
 * <h2>Thread-safety and implementations</h2>
 * Thread-safety depends on which factory method you use:
 * <ul>
 *   <li>{@link #of(Object)}: returns an atomic/CAS-capable implementation (thread-safe)</li>
 *   <li>{@link #volatileBox(Object)}: uses volatile semantics (visibility guarantees, but not atomic updates)</li>
 *   <li>{@link #plain(Object)}: no concurrency guarantees (fastest, single-thread usage)</li>
 * </ul>
 *
 * <p>
 * Some operations such as {@link #getAndUpdate(UnaryOperator)} and {@link #updateAndGet(UnaryOperator)}
 * rely on {@link #compareAndSet(Object, Object)} and therefore require a CAS-capable implementation.
 * If CAS is not supported, {@link #compareAndSet(Object, Object)} throws {@link UnsupportedOperationException}.
 *
 * <h2>Examples</h2>
 *
 * <h3>Using as a mutable holder</h3>
 * <pre>{@code
 * Box<Integer> counter = Box.plain(0);
 * counter.set(counter.get() + 1);
 * }</pre>
 *
 * <h3>Atomic update</h3>
 * <pre>{@code
 * Box<Integer> counter = Box.of(0);
 * counter.updateAndGet(n -> n + 1);
 * }</pre>
 *
 * <h3>Null-friendly convenience</h3>
 * <pre>{@code
 * String value = Box.plain(null).getOrElse("default");
 * }</pre>
 *
 * <h3>Read-only mapped view</h3>
 * <pre>{@code
 * Box<Integer> source = Box.of(10);
 * Box<String> view = source.view(Object::toString);
 * String s = view.get(); // "10"
 * }</pre>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>{@code Box} is intentionally small and composable.</li>
 *   <li>Null values are allowed.</li>
 *   <li>CAS support is optional and implementation-dependent.</li>
 *   <li>{@link BoxView} provides a lightweight read-only projection over a source box.</li>
 * </ul>
 *
 * @param <T> the value type stored in the box
 *
 * @see AtomicBox
 * @see PlainBox
 * @see AtomicVolatileBox
 */
public interface Box<T> {

    /**
     * Returns the current value stored in this box.
     * <p>
     * Note: thread-safety/visibility depends on the implementation.
     */
    T get();

    /**
     * Replaces the current value stored in this box.
     * <p>
     * Note: thread-safety/visibility depends on the implementation.
     */
    void set(T value);

    /**
     * Sets the value and returns the previous one.
     */
    default T getAndSet(T value) {
        T prev = get();
        set(value);
        return prev;
    }

    /**
     * @return {@code true} if {@link #get()} returns {@code null}.
     */
    default boolean isNull() {
        return get() == null;
    }

    /**
     * Converts the current value into an {@link Optional}.
     */
    default Optional<T> toOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Atomically sets the value to {@code update} if the current value equals {@code expect}.
     *
     * <p>
     * Implementations that do not support CAS should throw {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException if CAS is not supported by this box implementation.
     */
    default boolean compareAndSet(T expect, T update) {
        throw new UnsupportedOperationException("compareAndSet not supported");
    }

    /**
     * Atomically updates the value using the given function and returns the previous value.
     *
     * <p>
     * Requires CAS support. The update function may be invoked multiple times under contention.
     *
     * @throws UnsupportedOperationException if CAS is not supported.
     */
    default T getAndUpdate(UnaryOperator<T> updateFn) {
        Objects.requireNonNull(updateFn, "updateFn");
        while (true) {
            T prev = get();
            T next = updateFn.apply(prev);
            if (compareAndSet(prev, next)) return prev;
        }
    }

    /**
     * Atomically updates the value using the given function and returns the updated value.
     *
     * <p>
     * Requires CAS support. The update function may be invoked multiple times under contention.
     *
     * @throws UnsupportedOperationException if CAS is not supported.
     */
    default T updateAndGet(UnaryOperator<T> updateFn) {
        Objects.requireNonNull(updateFn, "updateFn");
        while (true) {
            T prev = get();
            T next = updateFn.apply(prev);
            if (compareAndSet(prev, next)) return next;
        }
    }

    /**
     * Updates the value using {@link #updateAndGet(UnaryOperator)} and ignores the return.
     */
    default void update(UnaryOperator<T> updateFn) {
        updateAndGet(updateFn);
    }

    /**
     * Returns the current value if non-null, otherwise returns {@code defaultValue}.
     */
    default T getOrElse(T defaultValue) {
        T v = get();
        return v != null ? v : defaultValue;
    }

    /**
     * Executes the consumer if the current value is non-null.
     */
    default void ifPresent(Consumer<? super T> c) {
        T v = get();
        if (v != null) c.accept(v);
    }

    /**
     * Creates a read-only mapped view of this box.
     * <p>
     * The returned box reflects the current source value when {@link BoxView#get()} is called.
     */
    default <R> BoxView<T, R> view(Function<? super T, ? extends R> mapper) {
        return new BoxView<>(this, mapper);
    }

    /**
     * Creates a CAS-capable (atomic) box.
     * Suitable for concurrent updates and operations that rely on {@link #compareAndSet(Object, Object)}.
     */
    @Contract("_ -> new")
    static <T> @NotNull Box<T> of(T initial) {
        return new AtomicBox<>(initial);
    }

    /**
     * Creates a plain (non-thread-safe) box with no concurrency guarantees.
     * Best suited for single-thread usage with minimal overhead.
     */
    @Contract("_ -> new")
    static <T> @NotNull Box<T> plain(T initial) {
        return new PlainBox<>(initial);
    }

    /**
     * Creates a volatile-based box.
     * Provides visibility guarantees across threads but does not provide atomic compound updates
     * unless the underlying implementation also supports CAS.
     */
    @Contract("_ -> new")
    static <T> @NotNull Box<T> volatileBox(T initial) {
        return new AtomicVolatileBox<>(initial);
    }

    /**
     * Read-only mapped view of a source {@link Box}.
     *
     * <p>
     * This view does not store a separate value; it delegates reads to the source box and
     * applies a mapping function on access.
     *
     * <p>
     * {@link #set(Object)} is not supported and always throws.
     */
    final class BoxView<S, T> implements Box<T> {
        private final Box<S> source;
        private final Function<? super S, ? extends T> mapper;

        public BoxView(Box<S> source, Function<? super S, ? extends T> mapper) {
            this.source = Objects.requireNonNull(source, "source");
            this.mapper = Objects.requireNonNull(mapper, "mapper");
        }

        @Override
        public T get() {
            return mapper.apply(source.get());
        }

        @Override
        public void set(T value) {
            throw new UnsupportedOperationException("view is read-only");
        }
    }
}