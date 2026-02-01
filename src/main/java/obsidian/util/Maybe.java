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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.NoSuchElementException;
import java.util.function.*;
import java.util.Optional;
import java.util.Objects;

/**
 * Represents an optional value that may either be present ({@code Some})
 * or absent ({@code None}), optionally carrying a reason for absence.
 *
 * <h2>Overview</h2>
 * {@code Maybe} is a functional container inspired by constructs such as:
 * <ul>
 *   <li>{@code Optional} (Java)</li>
 *   <li>{@code Option} (Scala)</li>
 *   <li>{@code Maybe} (Haskell)</li>
 * </ul>
 *
 * Unlike {@link Optional}, this implementation is designed to be used freely as:
 * <ul>
 *   <li>method parameters</li>
 *   <li>return types</li>
 *   <li>internal domain values</li>
 * </ul>
 *
 * <h2>Key differences vs {@link Optional}</h2>
 * <ul>
 *   <li>Safe and expressive return type for APIs</li>
 *   <li>Fluent functional API</li>
 *   <li>Optional absence reason for debugging and diagnostics</li>
 *   <li>No anti-pattern restrictions on usage</li>
 * </ul>
 *
 * <h2>Presence model</h2>
 * A {@code Maybe} instance is always in exactly one of two states:
 * <ul>
 *   <li>{@link Some} — contains a non-null value</li>
 *   <li>{@link None} — contains no value and may optionally carry a reason</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>Creation</h3>
 * <pre>{@code
 * Maybe<String> a = Maybe.some("hello");
 * Maybe<String> b = Maybe.none("not found");
 * Maybe<String> c = Maybe.of(nullableValue);
 * }</pre>
 *
 * <h3>Mapping</h3>
 * <pre>{@code
 * Maybe<Integer> len =
 *     Maybe.some("text")
 *          .map(String::length);
 * }</pre>
 *
 * <h3>Filtering</h3>
 * <pre>{@code
 * Maybe<Integer> even =
 *     Maybe.some(10)
 *          .filter(n -> n % 2 == 0);
 * }</pre>
 *
 * <h3>Side effects</h3>
 * <pre>{@code
 * maybe
 *     .onSome(v -> log.info("Value = " + v))
 *     .onNone(() -> log.warn("Missing value"));
 * }</pre>
 *
 * <h3>Fallback</h3>
 * <pre>{@code
 * String value = maybe.orElse("default");
 * }</pre>
 *
 * <h3>Exception bridging</h3>
 * <pre>{@code
 * String value = maybe.orElseThrow();
 * }</pre>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>{@code Maybe} never stores {@code null} as a value.</li>
 *   <li>{@code None} may optionally include a human-readable reason.</li>
 *   <li>All transformation methods preserve absence automatically.</li>
 *   <li>Methods are null-safe by contract unless explicitly stated.</li>
 * </ul>
 *
 * @param <T> the type of the contained value
 *
 * @see Optional
 */
public sealed interface Maybe<T> permits Maybe.Some, Maybe.None {

    /**
     * Indicates whether a value is present.
     *
     * @return {@code true} if this instance represents {@link Some}, {@code false} otherwise.
     */
    boolean isPresent();

    /**
     * Returns {@code true} if this instance represents {@link None}.
     */
    default boolean isEmpty() {
        return !isPresent();
    }

    /**
     * Returns the wrapped value if present.
     *
     * @throws NoSuchElementException if this instance represents {@link None}.
     */
    T get();

    /**
     * Returns an optional human-readable reason describing why the value is absent.
     * <p>
     * This value is meaningful only when this instance represents {@link None}.
     *
     * @return the absence reason, or {@code null} if none was provided.
     */
    default @Nullable String reason() {
        return null;
    }

    // -------------------- Factories --------------------

    /**
     * Creates a {@code Maybe} from a possibly-null value.
     *
     * <p>
     * If {@code value} is {@code null}, returns {@link #none()}.
     * Otherwise returns {@link #some(Object)}.
     */
    static <T> @NotNull Maybe<T> of(@Nullable T value) {
        return value == null ? none() : new Some<>(value);
    }

    /**
     * Creates a {@link Some} containing the given non-null value.
     */
    static <T> @NotNull Maybe<T> some(@NotNull T value) {
        return new Some<>(Objects.requireNonNull(value, "value"));
    }

    /**
     * Returns an empty {@code Maybe} with no reason.
     */
    @Contract(pure = true)
    static <T> @Unmodifiable @NotNull Maybe<T> none() {
        return None.instance();
    }

    /**
     * Returns an empty {@code Maybe} with the specified absence reason.
     */
    static <T> @NotNull Maybe<T> none(@NotNull String reason) {
        return new None<>(Objects.requireNonNull(reason, "reason"));
    }

    /**
     * Converts a Java {@link Optional} into a {@code Maybe}.
     */
    @SuppressWarnings("unchecked")
    static <T> @NotNull Maybe<T> from(@NotNull Optional<? extends T> optional) {
        Objects.requireNonNull(optional, "optional");
        return (Maybe<T>) optional.map(Maybe::some).orElseGet(Maybe::none);
    }

    // -------------------- Mapping --------------------

    /**
     * Transforms the contained value if present.
     * <p>
     * If this instance is {@link None}, the same absence is preserved.
     */
    default <U> @NotNull Maybe<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isEmpty()) return castNone();
        return Maybe.of(mapper.apply(get()));
    }

    /**
     * Flat-maps the contained value into another {@code Maybe}.
     */
    default <U> @NotNull Maybe<U> flatMap(@NotNull Function<? super T, ? extends Maybe<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isEmpty()) return castNone();
        return Objects.requireNonNull(mapper.apply(get()), "flatMap returned null");
    }

    /**
     * Filters the contained value using the given predicate.
     * <p>
     * If the predicate does not hold, the result becomes {@link None}.
     */
    default @NotNull Maybe<T> filter(@NotNull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        if (isEmpty()) return this;
        return predicate.test(get()) ? this : none("Predicate does not hold");
    }

    /**
     * Like Stream.peek(): executes side-effect if present, but returns itself.
     */
    default @NotNull Maybe<T> peek(@NotNull Consumer<? super T> action) {
        Objects.requireNonNull(action, "action");
        if (isPresent()) action.accept(get());
        return this;
    }

    // -------------------- Side-effect API --------------------

    /**
     * Executes the given action if a value is present.
     * Returns this instance unchanged.
     */
    default @NotNull Maybe<T> onSome(@NotNull Consumer<? super T> action) {
        Objects.requireNonNull(action, "action");
        if (isPresent()) action.accept(get());
        return this;
    }

    /**
     * Executes the given action if this instance is empty.
     * Returns this instance unchanged.
     */
    default @NotNull Maybe<T> onNone(@NotNull Runnable action) {
        Objects.requireNonNull(action, "action");
        if (isEmpty()) action.run();
        return this;
    }

    // -------------------- Fallbacks --------------------

    /**
     * Returns the contained value or {@code null} if absent.
     */
    default @Nullable T orNull() {
        return isPresent() ? get() : null;
    }

    /**
     * Returns the contained value or the provided fallback.
     */
    default @NotNull T orElse(@NotNull T fallback) {
        Objects.requireNonNull(fallback, "fallback");
        return isPresent() ? get() : fallback;
    }

    default @Nullable T orElseNullable(@Nullable T fallback) {
        return isPresent() ? get() : fallback;
    }

    default @NotNull T orElseGet(@NotNull Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (isPresent()) return get();
        T v = supplier.get();
        return Objects.requireNonNull(v, "orElseGet supplier returned null");
    }

    /**
     * Returns the contained value or throws a supplied exception.
     */
    default <X extends RuntimeException> @NotNull T orElseThrow(@NotNull Supplier<X> exSupplier) {
        Objects.requireNonNull(exSupplier, "exSupplier");
        if (isPresent()) return get();
        throw exSupplier.get();
    }

    /**
     * Returns the contained value or throws {@link NoSuchElementException}.
     */
    default @NotNull T orElseThrow() {
        if (isPresent()) return get();
        String r = reason();
        throw new NoSuchElementException(r == null ? "Maybe is empty" : "Maybe is empty: " + r);
    }

    // -------------------- Conversions --------------------

    default @NotNull Optional<T> toOptional() {
        return isPresent() ? Optional.ofNullable(get()) : Optional.empty();
    }

    // -------------------- Utilities --------------------

    @SuppressWarnings("unchecked")
    private <U> Maybe<U> castNone() {
        return (Maybe<U>) this;
    }

    // -------------------- Implementations --------------------

    /**
     * Represents the presence of a non-null value.
     */
    record Some<T>(@NotNull T value) implements Maybe<T> {
        public Some {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public @NotNull String toString() {
            return "Some[" + value + "]";
        }
    }

    /**
     * Represents the absence of a value.
     * <p>
     * A {@code None} instance may optionally contain a descriptive reason.
     */
    record None<T>(@Nullable String reason) implements Maybe<T> {

        private static final None<?> EMPTY = new None<>(null);

        @SuppressWarnings("unchecked")
        static <T> None<T> instance() {
            return (None<T>) EMPTY;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T get() {
            String r = reason;
            throw new NoSuchElementException(r == null ? "Maybe is empty" : "Maybe is empty: " + r);
        }

        @Override
        public @NotNull String toString() {
            return reason == null ? "None" : "None[" + reason + "]";
        }
    }
}