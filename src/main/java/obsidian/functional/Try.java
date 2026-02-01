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

package obsidian.functional;

import obsidian.functional.exceptions.TryGetOrFailureException;
import obsidian.functional.failable.FailableConsumer;
import obsidian.functional.failable.FailableFunction;
import obsidian.functional.failable.FailableRunnable;
import obsidian.functional.failable.FailableSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a computation that may either succeed with a value ({@link Success})
 * or fail with a {@link Throwable} ({@link Failure}).
 *
 * <h2>Overview</h2>
 * {@code Try} is a functional error-handling container designed to replace explicit
 * {@code try/catch} blocks in fluent pipelines.
 *
 * <p>
 * A {@code Try<T>} instance is always in exactly one of two states:
 * <ul>
 *   <li>{@link Success} — contains a value (may be {@code null}, depending on how it was created)</li>
 *   <li>{@link Failure} — contains an error ({@link Throwable})</li>
 * </ul>
 *
 * <h2>Why use Try?</h2>
 * {@code Try} makes it easy to:
 * <ul>
 *   <li>chain computations ({@link #map(Function)}, {@link #flatMap(Function)})</li>
 *   <li>recover from failures ({@link #recover(Function)}, {@link #recoverWith(Function)})</li>
 *   <li>execute side effects safely ({@link #andThen(Consumer)}, {@link #peek(Consumer)})</li>
 *   <li>bridge back into exceptions when needed ({@link #getOrThrow(Function)}, {@link #checkedGet()})</li>
 * </ul>
 *
 * <h2>Try vs Optional</h2>
 * {@link Optional} models presence/absence, while {@code Try} models success/failure.
 * A failure always carries the underlying error, making debugging and recovery explicit.
 *
 * <h2>Examples</h2>
 *
 * <h3>Capture exceptions from a supplier</h3>
 * <pre>{@code
 * Try<Integer> t = Try.of(() -> Integer.parseInt("123"));
 * }</pre>
 *
 * <h3>Mapping</h3>
 * <pre>{@code
 * Try<Integer> length = Try.success("hello").map(String::length);
 * }</pre>
 *
 * <h3>Flat-mapping (compose Try results)</h3>
 * <pre>{@code
 * Try<Integer> parsed =
 *     Try.of(() -> "10")
 *        .flatMap(s -> Try.of(() -> Integer.parseInt(s)));
 * }</pre>
 *
 * <h3>Recover from failures</h3>
 * <pre>{@code
 * int v = Try.of(() -> Integer.parseInt("x"))
 *            .recover(err -> 0)
 *            .getOrElse(0);
 * }</pre>
 *
 * <h3>Side-effects without breaking the pipeline</h3>
 * <pre>{@code
 * Try.of(() -> "data")
 *    .peek(System.out::println)
 *    .onFailure(Throwable::printStackTrace);
 * }</pre>
 *
 * <h3>Try-with-resources helper</h3>
 * <pre>{@code
 * Function<InputStream, Try<String>> readAll =
 *     Try.withResources(in -> new String(in.readAllBytes(), StandardCharsets.UTF_8));
 * }</pre>
 *
 * <h2>Accessing values</h2>
 * <ul>
 *   <li>{@link #get()} returns the value for {@link Success} and throws for {@link Failure}.</li>
 *   <li>{@link #getOrElse(Object)} provides a fallback value.</li>
 *   <li>{@link #checkedGet()} rethrows the original checked exception when possible.</li>
 * </ul>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>{@code Try} is immutable.</li>
 *   <li>All transformations preserve failures automatically.</li>
 *   <li>{@link Failure#get()} throws a dedicated unchecked exception for fast failure propagation.</li>
 * </ul>
 *
 * @param <T> the success value type
 *
 * @see Success
 * @see Failure
 */
public abstract class Try<T> {


    private Try() { }

    /**
     * Creates a successful {@code Try} holding the given value.
     */
    @Contract("_ -> new")
    public static <T> @NotNull Try<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed {@code Try} holding the given error.
     *
     * @throws NullPointerException if {@code error} is null.
     */
    @Contract("_ -> new")
    public static <T> @NotNull Try<T> failure(Throwable error) {
        return new Failure<>(Objects.requireNonNull(error, "error"));
    }

    /**
     * Executes the supplier and captures any thrown exception into a {@link Failure}.
     *
     * <p>
     * {@link Error}s are rethrown to avoid masking fatal JVM errors.
     */
    public static <T> Try<T> of(FailableSupplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier");

        try {
            return success(supplier.get());
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            return failure(e);
        } catch (Throwable t) {
            return failure(t);
        }
    }

    /**
     * Executes the runnable and captures any thrown exception into a {@link Failure}.
     */
    public static Try<Void> run(FailableRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable");

        return Try.of(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Returns a function that executes the given operation using try-with-resources,
     * capturing failures into {@code Try}.
     *
     * <p>
     * The returned function will close the resource automatically.
     */
    @Contract(pure = true)
    public static <T extends AutoCloseable, R> @NotNull Function<T, Try<R>> withResources(
            Function<T, R> fn
    ) {
        Objects.requireNonNull(fn, "fn");

        return resource -> Try.of(() -> {
            try (T r = resource) {
                return fn.apply(r);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> Try<T> flatten(Try<? extends Try<? extends T>> nested) {
        Objects.requireNonNull(nested, "nested");
        return (Try<T>) nested.flatMap(x -> (Try<T>) x);
    }

    /**
     * @return {@code true} if this instance represents {@link Success}.
     */
    public abstract boolean isSuccess();

    /**
     * @return {@code true} if this instance represents {@link Failure}.
     */
    public final boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Returns the success value.
     *
     * @throws RuntimeException if this instance represents {@link Failure}.
     */
    public abstract T get();

    /**
     * Returns the failure error if present.
     *
     * @return an optional containing the error for {@link Failure}, or empty for {@link Success}.
     */
    public abstract Optional<Throwable> exception();

    /**
     * Returns the value if successful, otherwise rethrows the underlying error as:
     * <ul>
     *   <li>the same checked {@link Exception}, if applicable</li>
     *   <li>the same {@link Error}, if applicable</li>
     *   <li>otherwise wrapped in a {@link RuntimeException}</li>
     * </ul>
     */
    public final T checkedGet() throws Exception {
        if (isSuccess()) return get();

        Throwable t = exception().orElse(new NoSuchElementException("Failure without exception"));
        if (t instanceof Exception) throw (Exception) t;
        if (t instanceof Error) throw (Error) t;

        throw new RuntimeException(t);
    }

    /**
     * Executes the given action if successful.
     */
    public abstract void forEach(Consumer<? super T> action);

    /**
     * Maps the success value, preserving failures.
     */
    public abstract <U> Try<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Flat-maps the success value into another {@code Try}, preserving failures.
     */
    public abstract <U> Try<U> flatMap(Function<? super T, ? extends Try<? extends U>> mapper);

    /**
     * Like {@link #map(Function)}, but allows checked exceptions inside the mapper.
     */
    public final <U> Try<U> mapTry(FailableFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return flatMap(v -> Try.of(() -> mapper.apply(v)));
    }

    /**
     * Converts {@link Success} into {@link Failure} if the predicate does not hold.
     */
    public abstract Try<T> filter(Predicate<? super T> predicate);

    /**
     * Recovers from failure by converting the error into a fallback value.
     */
    public abstract <U> Try<U> recover(Function<? super Throwable, ? extends U> recoverFunc);

    /**
     * Recovers from failure by converting the error into another {@code Try}.
     */
    public abstract <U> Try<U> recoverWith(Function<? super Throwable, ? extends Try<? extends U>> recoverFunc);

    /**
     * Type-based recovery helper. Only recovers if the failure error is an instance of {@code type}.
     */
    public final <E extends Throwable> Try<T> recover(
            Class<E> type,
            Function<? super E, ? extends T> recoverFunc
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(recoverFunc, "recoverFunc");

        if (isSuccess()) return this;

        Throwable err = exception().orElse(null);
        if (err != null && type.isInstance(err)) {
            return Try.of(() -> recoverFunc.apply(type.cast(err)));
        }
        return this;
    }

    /**
     * For {@link Failure}, returns {@link Success} containing the error.
     * For {@link Success}, returns {@link Failure} indicating misuse.
     */
    public abstract Try<Throwable> failed();

    /**
     * Converts a successful value into {@link Optional}; failures become {@link Optional#empty()}.
     */
    public abstract Optional<T> toOptional();

    /**
     * Returns the value if successful, otherwise returns {@code defaultValue}.
     */
    public abstract T getOrElse(T defaultValue);

    /**
     * Returns this instance if successful, otherwise returns {@code defaultValue}.
     */
    public abstract Try<T> orElse(Try<? extends T> defaultValue);

    /**
     * Transforms this {@code Try} by providing handlers for both success and failure.
     */
    public abstract <U> Try<U> transform(
            Function<? super T, ? extends Try<? extends U>> successFunc,
            Function<? super Throwable, ? extends Try<? extends U>> failureFunc
    );

    /**
     * Runs the given consumer if successful, returning the same successful value.
     * If the consumer throws, the result becomes a {@link Failure}.
     */
    public final Try<T> andThen(FailableConsumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer");

        return flatMap(v -> Try.of(() -> {
            consumer.accept(v);
            return v;
        }));
    }

    public final Try<T> andThen(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return andThen((FailableConsumer<? super T>) consumer::accept);
    }

    /**
     * Like Stream.peek(): executes an action on success without changing the result.
     */
    public final Try<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action");
        forEach(action);
        return this;
    }

    /**
     * Executes an action if this instance is a failure.
     */
    public final Try<T> peekFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action");
        exception().ifPresent(action);
        return this;
    }

    public final Try<T> onFailure(Consumer<? super Throwable> action) {
        return peekFailure(action);
    }

    /**
     * Folds this result into a single value by mapping either success or failure.
     */
    public final <U> U fold(
            Function<? super T, ? extends U> onSuccess,
            Function<? super Throwable, ? extends U> onFailure
    ) {
        Objects.requireNonNull(onSuccess, "onSuccess");
        Objects.requireNonNull(onFailure, "onFailure");

        return isSuccess()
                ? onSuccess.apply(get())
                : onFailure.apply(exception().orElse(null));
    }

    /**
     * Maps the error of a failure into another error.
     */
    public final Try<T> mapFailure(Function<? super Throwable, ? extends Throwable> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isSuccess()) return this;

        Throwable err = exception().orElse(null);
        Throwable mapped = mapper.apply(err);

        if (mapped == null) mapped = new NullPointerException("mapFailure returned null");
        return Try.failure(mapped);
    }

    /**
     * Returns the value if successful; otherwise throws a mapped {@link RuntimeException}.
     */
    public final T getOrThrow(Function<? super Throwable, ? extends RuntimeException> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isSuccess()) return get();

        Throwable err = exception().orElse(null);
        RuntimeException ex = mapper.apply(err);

        if (ex == null) ex = new RuntimeException(err);
        throw ex;
    }

    /**
     * Successful {@code Try} variant holding a value.
     */
    public static final class Success<T> extends Try<T> {

        private final T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Contract(pure = true)
        @Override
        public @NotNull Optional<Throwable> exception() {
            return Optional.empty();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            Objects.requireNonNull(action, "action");
            action.accept(value);
        }

        @Override
        public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
            Objects.requireNonNull(mapper, "mapper");
            return Try.of(() -> mapper.apply(value));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> flatMap(Function<? super T, ? extends Try<? extends U>> mapper) {
            Objects.requireNonNull(mapper, "mapper");

            return Try.of(() -> mapper.apply(value)).transform(
                    x -> (Try<U>) x,
                    Try::failure
            );
        }

        @Override
        public Try<T> filter(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate");

            if (predicate.test(value)) return this;
            return Try.failure(new NoSuchElementException("Predicate does not hold for " + value));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> recover(Function<? super Throwable, ? extends U> recoverFunc) {
            return (Try<U>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> recoverWith(Function<? super Throwable, ? extends Try<? extends U>> recoverFunc) {
            return (Try<U>) this;
        }

        @Contract(" -> new")
        @Override
        public @NotNull Try<Throwable> failed() {
            return Try.failure(new UnsupportedOperationException("Success.failed"));
        }

        @Contract(pure = true)
        @Override
        public @NotNull Optional<T> toOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public Try<T> orElse(Try<? extends T> defaultValue) {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> transform(
                Function<? super T, ? extends Try<? extends U>> successFunc,
                Function<? super Throwable, ? extends Try<? extends U>> failureFunc
        ) {
            Objects.requireNonNull(successFunc, "successFunc");

            return Try.of(() -> successFunc.apply(value)).transform(
                    x -> (Try<U>) x,
                    Try::failure
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Success<?> success)) return false;
            return Objects.equals(value, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "Success{value=" + value + "}";
        }
    }

    /**
     * Failed {@code Try} variant holding an error.
     * <p>
     * Calling {@link #get()} throws {@link TryGetOrFailureException}.
     */
    public static final class Failure<T> extends Try<T> {

        private final Throwable               error;
        private final TryGetOrFailureException unchecked;

        private Failure(Throwable error) {
            this.error = Objects.requireNonNull(error, "error");
            this.unchecked = new TryGetOrFailureException(error);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T get() {
            throw unchecked;
        }

        @Contract(value = " -> new", pure = true)
        @Override
        public @NotNull Optional<Throwable> exception() {
            return Optional.of(error);
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            /* no-op */
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
            return (Try<U>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> flatMap(Function<? super T, ? extends Try<? extends U>> mapper) {
            return (Try<U>) this;
        }

        @Override
        public Try<T> filter(Predicate<? super T> predicate) {
            return this;
        }

        @Override
        public <U> Try<U> recover(Function<? super Throwable, ? extends U> recoverFunc) {
            Objects.requireNonNull(recoverFunc, "recoverFunc");
            return Try.of(() -> recoverFunc.apply(error));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> recoverWith(Function<? super Throwable, ? extends Try<? extends U>> recoverFunc) {
            Objects.requireNonNull(recoverFunc, "recoverFunc");

            return Try.of(() -> recoverFunc.apply(error)).transform(
                    x -> (Try<U>) x,
                    Try::failure
            );
        }

        @Contract(" -> new")
        @Override
        public @NotNull Try<Throwable> failed() {
            return Try.success(error);
        }

        @Contract(pure = true)
        @Override
        public @NotNull Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Try<T> orElse(Try<? extends T> defaultValue) {
            return (Try<T>) Objects.requireNonNull(defaultValue, "defaultValue");
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> transform(
                Function<? super T, ? extends Try<? extends U>> successFunc,
                Function<? super Throwable, ? extends Try<? extends U>> failureFunc
        ) {
            Objects.requireNonNull(failureFunc, "failureFunc");

            return Try.of(() -> failureFunc.apply(error)).transform(
                    x -> (Try<U>) x,
                    Try::failure
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Failure<?> failure)) return false;
            return Objects.equals(error, failure.error);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(error);
        }

        @Override
        public String toString() {
            return "Failure{error=" + error + "}";
        }
    }
}
