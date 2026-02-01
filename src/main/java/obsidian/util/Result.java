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

package obsidian.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

/**
 * Algebraic data type representing a computation result: either {@code Ok(value)} or {@code Err(error)}.
 *
 * <h2>Overview</h2>
 * {@code Result} is a lightweight alternative to exceptions and {@link Optional} for APIs that need to
 * return either a value or a well-defined error type.
 *
 * <p>
 * It is inspired by "Result" types from functional languages and Rust, allowing callers to:
 * <ul>
 *   <li>Handle success and failure explicitly</li>
 *   <li>Compose computations via {@link #map(Function)} and {@link #flatMap(Function)}</li>
 *   <li>Transform errors via {@link #mapError(Function)}</li>
 *   <li>Convert to a single value via {@link #fold(Function, Function)}</li>
 * </ul>
 *
 * <h2>Differences vs {@link Optional}</h2>
 * {@code Optional} represents "present / empty" without an error payload.
 * {@code Result} represents "success / failure" with an explicit error type {@code E}.
 *
 * <ul>
 *   <li>{@code Optional<T>} -> maybe a value</li>
 *   <li>{@code Result<T, E>} -> either a value or an error</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>Basic usage</h3>
 * <pre>{@code
 * Result<Integer, String> r = Result.ok(10);
 * int v = r.orElse(0); // 10
 * }</pre>
 *
 * <h3>Propagating errors with map/flatMap</h3>
 * <pre>{@code
 * Result<Integer, String> parsed =
 *     parseInt("123")
 *         .map(n -> n * 2);          // Ok(246) or Err(...)
 *
 * Result<Integer, String> chained =
 *     parseInt("10")
 *         .flatMap(n -> divide(100, n)); // propagates Err automatically
 * }</pre>
 *
 * <h3>Handling both branches with fold</h3>
 * <pre>{@code
 * String msg = result.fold(
 *     ok  -> "Value = " + ok,
 *     err -> "Error: " + err
 * );
 * }</pre>
 *
 * <h3>Capturing exceptions into Result</h3>
 * <pre>{@code
 * Result<String, String> r = Result.from(
 *     () -> Files.readString(path),
 *     t  -> "I/O failed: " + t.getMessage()
 * );
 * }</pre>
 *
 * <h2>Nullability</h2>
 * <ul>
 *   <li>{@link Ok} may hold a {@code null} value (useful for "success with no payload")</li>
 *   <li>{@link Err#error()} is always non-null</li>
 * </ul>
 *
 * @param <T> success value type
 * @param <E> error type
 *
 * @see Optional
 */
public sealed interface Result<T, E> permits Result.Ok, Result.Err {

    // ---------- Variants ----------

    /**
     * Success variant. The wrapped value may be {@code null}.
     *
     * @param value the success value (nullable)
     */
    record Ok<T, E>(@Nullable T value) implements Result<T, E> { }

    /**
     * Failure variant. The wrapped error is always non-null.
     *
     * @param error the error payload (non-null)
     */
    record Err<T, E>(@NotNull E error) implements Result<T, E> {
        public Err {
            Objects.requireNonNull(error, "error");
        }
    }

    // ---------- Factories ----------

    /**
     * Creates a success result.
     *
     * @param value the success value (may be {@code null})
     */
    static <T, E> @NotNull Result<T, E> ok(@Nullable T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failure result.
     *
     * @param error the error payload (must not be {@code null})
     */
    static <T, E> @NotNull Result<T, E> err(@NotNull E error) {
        return new Err<>(error);
    }

    /**
     * Executes a supplier and captures any thrown exception as {@code Err} using {@code errorMapper}.
     *
     * <p>
     * This is useful to bridge exception-based APIs into a {@code Result}-based workflow.
     *
     * @param supplier the code to execute
     * @param errorMapper maps the captured {@link Throwable} into an error value
     * @return {@code Ok(value)} if the supplier succeeds; {@code Err(mappedError)} otherwise
     */
    static <T, E> @NotNull Result<T, E> from(
            @NotNull CheckedSupplier<? extends T> supplier,
            @NotNull Function<? super Throwable, ? extends E> errorMapper
    ) {
        Objects.requireNonNull(supplier, "supplier");
        Objects.requireNonNull(errorMapper, "errorMapper");
        try {
            return ok(supplier.get());
        } catch (Throwable t) {
            return err(errorMapper.apply(t));
        }
    }

    // ---------- State ----------

    /**
     * @return {@code true} if this result is {@link Ok}.
     */
    default boolean isOk() {
        return this instanceof Ok<?, ?>;
    }

    /**
     * @return {@code true} if this result is {@link Err}.
     */
    default boolean isErr() {
        return this instanceof Err<?, ?>;
    }

    // ---------- Unwrap ----------

    /**
     * Returns the success value if {@link Ok}, or {@code null} if {@link Err}.
     *
     * <p>This method does not differentiate between "Ok(null)" and "Err(...)".</p>
     *
     * @return the success value, or {@code null} on error
     */
    default @Nullable T orNull() {
        return switch (this) {
            case Ok<T, E> ok -> ok.value();
            case Err<T, E> err -> null;
        };
    }

    /**
     * Converts this result into an {@link Optional} containing the success value (if any).
     *
     * @return an optional with the {@link Ok} value, or empty if {@link Err}
     */
    default Optional<T> toOptional() {
        return Optional.ofNullable(orNull());
    }

    /**
     * Returns the success value if {@link Ok}, otherwise returns {@code fallback}.
     *
     * @param fallback value to use on error
     */
    default T orElse(T fallback) {
        return isOk() ? ((Ok<T, E>) this).value() : fallback;
    }

    /**
     * Returns the success value if {@link Ok}, otherwise computes a fallback value.
     *
     * @param fallback supplier of fallback value (non-null)
     */
    default T orElseGet(@NotNull Supplier<? extends T> fallback) {
        Objects.requireNonNull(fallback, "fallback");
        return switch (this) {
            case Ok<T, E> ok -> ok.value();
            case Err<T, E> err -> fallback.get();
        };
    }

    /**
     * Returns the success value if {@link Ok}, otherwise throws {@link IllegalStateException}
     * using {@code String.valueOf(error)} as message.
     */
    default T orElseThrow() {
        return orElseThrow(e -> new IllegalStateException(String.valueOf(e)));
    }

    /**
     * Returns the success value if {@link Ok}, otherwise throws an exception produced by {@code exMapper}.
     *
     * @param exMapper maps the error value into a runtime exception
     * @throws RuntimeException produced by {@code exMapper} when this is {@link Err}
     */
    default T orElseThrow(@NotNull Function<? super E, ? extends RuntimeException> exMapper) {
        Objects.requireNonNull(exMapper, "exMapper");
        return switch (this) {
            case Ok<T, E> ok -> ok.value();
            case Err<T, E> err -> throw exMapper.apply(err.error());
        };
    }

    /**
     * Returns the error value if {@link Err}, otherwise throws.
     *
     * @return the error payload
     * @throws IllegalStateException if this is {@link Ok}
     */
    default @NotNull E errorOrThrow() {
        return switch (this) {
            case Ok<T, E> ok -> throw new IllegalStateException("Result is Ok");
            case Err<T, E> err -> err.error();
        };
    }

    // ---------- Functional ops ----------

    /**
     * Maps the {@link Ok} value into another value, preserving {@link Err} as-is.
     *
     * @param fn mapping function applied only when {@link Ok}
     * @param <U> new success type
     * @return mapped {@code Ok}, or the original {@code Err}
     */
    default <U> @NotNull Result<U, E> map(@NotNull Function<? super T, ? extends U> fn) {
        Objects.requireNonNull(fn, "fn");
        return switch (this) {
            case Ok<T, E> ok -> Result.ok(fn.apply(ok.value()));
            case Err<T, E> err -> Result.err(err.error());
        };
    }

    /**
     * Flat-maps the {@link Ok} value into another {@code Result}, preserving {@link Err} as-is.
     *
     * @param fn function returning a {@code Result} applied only when {@link Ok}
     * @param <U> new success type
     * @return the returned result, or the original {@code Err}
     * @throws NullPointerException if {@code fn} returns {@code null}
     */
    default <U> @NotNull Result<U, E> flatMap(@NotNull Function<? super T, ? extends Result<U, E>> fn) {
        Objects.requireNonNull(fn, "fn");
        return switch (this) {
            case Ok<T, E> ok -> Objects.requireNonNull(fn.apply(ok.value()), "flatMap returned null");
            case Err<T, E> err -> Result.err(err.error());
        };
    }

    /**
     * Maps the {@link Err} value into another error type, preserving {@link Ok} as-is.
     *
     * @param fn mapping function applied only when {@link Err}
     * @param <F> new error type
     * @return the same {@code Ok} value, or a mapped {@code Err}
     */
    default <F> @NotNull Result<T, F> mapError(@NotNull Function<? super E, ? extends F> fn) {
        Objects.requireNonNull(fn, "fn");
        return switch (this) {
            case Ok<T, E> ok -> Result.ok(ok.value());
            case Err<T, E> err -> Result.err(fn.apply(err.error()));
        };
    }

    /**
     * Executes a side-effect if this is {@link Ok}, returning the same result.
     * Similar to {@code Stream.peek()}.
     *
     * @param c action to run on success
     * @return this result unchanged
     */
    default @NotNull Result<T, E> tap(@NotNull Consumer<? super T> c) {
        Objects.requireNonNull(c, "c");
        if (this instanceof Ok<T, E>(T value)) c.accept(value);
        return this;
    }

    /**
     * Executes a side-effect if this is {@link Err}, returning the same result.
     *
     * @param c action to run on error
     * @return this result unchanged
     */
    default @NotNull Result<T, E> tapError(@NotNull Consumer<? super E> c) {
        Objects.requireNonNull(c, "c");
        if (this instanceof Err<T, E>(E error)) c.accept(error);
        return this;
    }

    /**
     * Converts an {@link Err} into {@link Ok} by mapping the error into a fallback value.
     * If already {@link Ok}, returns itself.
     *
     * @param fn maps the error into a success value
     * @return {@code Ok(value)} (either original or recovered)
     */
    default @NotNull Result<T, E> recover(@NotNull Function<? super E, ? extends T> fn) {
        Objects.requireNonNull(fn, "fn");
        return switch (this) {
            case Ok<T, E> ok -> this;
            case Err<T, E> err -> Result.ok(fn.apply(err.error()));
        };
    }

    /**
     * Converts an {@link Err} into another {@code Result} (possibly still {@link Err}).
     * If already {@link Ok}, returns itself.
     *
     * @param fn maps the error into a {@code Result}
     * @return original {@code Ok}, or recovered result
     * @throws NullPointerException if {@code fn} returns {@code null}
     */
    default @NotNull Result<T, E> recoverWith(@NotNull Function<? super E, ? extends Result<T, E>> fn) {
        Objects.requireNonNull(fn, "fn");
        return switch (this) {
            case Ok<T, E> ok -> this;
            case Err<T, E> err -> Objects.requireNonNull(fn.apply(err.error()), "recoverWith returned null");
        };
    }

    /**
     * Folds this result into a single value by handling both branches explicitly.
     *
     * @param onOk function applied to the success value
     * @param onErr function applied to the error value
     * @param <R> fold return type
     * @return the value produced by {@code onOk} or {@code onErr}
     */
    default <R> R fold(@NotNull Function<? super T, ? extends R> onOk,
                       @NotNull Function<? super E, ? extends R> onErr) {
        Objects.requireNonNull(onOk, "onOk");
        Objects.requireNonNull(onErr, "onErr");
        return switch (this) {
            case Ok<T, E> ok -> onOk.apply(ok.value());
            case Err<T, E> err -> onErr.apply(err.error());
        };
    }

    // ---------- Checked supplier ----------

    /**
     * Supplier that may throw any {@link Throwable}.
     * Intended to be used with {@link #from(CheckedSupplier, Function)} to capture failures.
     */
    @FunctionalInterface
    interface CheckedSupplier<T> {
        T get() throws Throwable;
    }
}
