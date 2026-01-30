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
 * The {@code Try} class represents a computation that may either result in a successful value
 * or a failure (represented by a {@code Throwable}).
 * It provides an abstraction for error handling, avoiding the need for explicit
 * try-catch blocks.
 *
 * @param <T> The type of the value held by {@code Try} in the case of success.
 */
public abstract class Try<T> {

    private Try() { }

    @Contract("_ -> new")
    public static <T> @NotNull Try<T> success(T value) {
        return new Success<>(value);
    }

    @Contract("_ -> new")
    public static <T> @NotNull Try<T> failure(Throwable error) {
        return new Failure<>(Objects.requireNonNull(error, "error"));
    }

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

    public static Try<Void> run(FailableRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable");

        return Try.of(() -> {
            runnable.run();
            return null;
        });
    }

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

    public abstract boolean isSuccess();

    public final boolean isFailure() {
        return !isSuccess();
    }

    public abstract T get();

    public abstract Optional<Throwable> exception();

    public final T checkedGet() throws Exception {
        if (isSuccess()) return get();

        Throwable t = exception().orElse(new NoSuchElementException("Failure without exception"));
        if (t instanceof Exception) throw (Exception) t;
        if (t instanceof Error) throw (Error) t;

        throw new RuntimeException(t);
    }

    public abstract void forEach(Consumer<? super T> action);

    public abstract <U> Try<U> map(Function<? super T, ? extends U> mapper);

    public abstract <U> Try<U> flatMap(Function<? super T, ? extends Try<? extends U>> mapper);

    public final <U> Try<U> mapTry(FailableFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return flatMap(v -> Try.of(() -> mapper.apply(v)));
    }

    public abstract Try<T> filter(Predicate<? super T> predicate);

    public abstract <U> Try<U> recover(Function<? super Throwable, ? extends U> recoverFunc);

    public abstract <U> Try<U> recoverWith(Function<? super Throwable, ? extends Try<? extends U>> recoverFunc);

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

    public abstract Try<Throwable> failed();

    public abstract Optional<T> toOptional();

    public abstract T getOrElse(T defaultValue);

    public abstract Try<T> orElse(Try<? extends T> defaultValue);

    public abstract <U> Try<U> transform(
            Function<? super T, ? extends Try<? extends U>> successFunc,
            Function<? super Throwable, ? extends Try<? extends U>> failureFunc
    );

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

    public final Try<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action");
        forEach(action);
        return this;
    }

    public final Try<T> peekFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action");
        exception().ifPresent(action);
        return this;
    }

    public final Try<T> onFailure(Consumer<? super Throwable> action) {
        return peekFailure(action);
    }

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

    public final Try<T> mapFailure(Function<? super Throwable, ? extends Throwable> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isSuccess()) return this;

        Throwable err = exception().orElse(null);
        Throwable mapped = mapper.apply(err);

        if (mapped == null) mapped = new NullPointerException("mapFailure returned null");
        return Try.failure(mapped);
    }

    public final T getOrThrow(Function<? super Throwable, ? extends RuntimeException> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isSuccess()) return get();

        Throwable err = exception().orElse(null);
        RuntimeException ex = mapper.apply(err);

        if (ex == null) ex = new RuntimeException(err);
        throw ex;
    }

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
