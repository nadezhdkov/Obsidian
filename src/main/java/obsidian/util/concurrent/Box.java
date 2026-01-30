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
import obsidian.util.concurrent.atomic.VolatileBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

public interface Box<T> {

    T get();

    void set(T value);

    default T getAndSet(T value) {
        T prev = get();
        set(value);
        return prev;
    }

    default boolean isNull() {
        return get() == null;
    }

    default Optional<T> toOptional() {
        return Optional.ofNullable(get());
    }

    default boolean compareAndSet(T expect, T update) {
        throw new UnsupportedOperationException("compareAndSet not supported");
    }

    default T getAndUpdate(UnaryOperator<T> updateFn) {
        Objects.requireNonNull(updateFn, "updateFn");
        while (true) {
            T prev = get();
            T next = updateFn.apply(prev);
            if (compareAndSet(prev, next)) return prev;
        }
    }

    default T updateAndGet(UnaryOperator<T> updateFn) {
        Objects.requireNonNull(updateFn, "updateFn");
        while (true) {
            T prev = get();
            T next = updateFn.apply(prev);
            if (compareAndSet(prev, next)) return next;
        }
    }

    default void update(UnaryOperator<T> updateFn) {
        updateAndGet(updateFn);
    }

    default T getOrElse(T defaultValue) {
        T v = get();
        return v != null ? v : defaultValue;
    }

    default void ifPresent(Consumer<? super T> c) {
        T v = get();
        if (v != null) c.accept(v);
    }

    default <R> BoxView<T, R> view(Function<? super T, ? extends R> mapper) {
        return new BoxView<>(this, mapper);
    }

    @Contract("_ -> new")
    static <T> @NotNull Box<T> atomic(T initial) {
        return new AtomicBox<>(initial);
    }

    @Contract("_ -> new")
    static <T> @NotNull Box<T> plain(T initial) {
        return new PlainBox<>(initial);
    }

    @Contract("_ -> new")
    static <T> @NotNull Box<T> volatileBox(T initial) {
        return new VolatileBox<>(initial);
    }

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