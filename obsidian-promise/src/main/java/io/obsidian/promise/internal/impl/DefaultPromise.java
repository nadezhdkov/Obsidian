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

package io.obsidian.promise.internal.impl;

import io.obsidian.promise.api.*;
import io.obsidian.promise.error.TimeoutException;
import io.obsidian.promise.util.concurrent.PromiseCompletableFuture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.*;

public class DefaultPromise<T> implements Promise<T> {

    private final CompletableFuture<T>     future;
    private final PromiseCompletableFuture engine;

    private DefaultPromise(CompletableFuture<T> future) {
        this.future = future;
        this.engine = PromiseCompletableFuture.getInstance();
    }

    // ==================== Factory Methods ====================

    @Contract("_ -> new")
    public static <T> @NotNull DefaultPromise<T> resolved(T value) {
        return new DefaultPromise<>(CompletableFuture.completedFuture(value));
    }

    @Contract("_ -> new")
    public static <T> @NotNull DefaultPromise<T> rejected(Throwable error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return new DefaultPromise<>(future);
    }

    @Contract(" -> new")
    public static <T> @NotNull DefaultPromise<T> cancelled() {
        return cancelled("Cancelled");
    }

    @Contract("_ -> new")
    public static <T> @NotNull DefaultPromise<T> cancelled(String reason) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(new io.obsidian.promise.error.CancellationException(reason));
        return new DefaultPromise<>(future);
    }

    public static <T> @NotNull DefaultPromise<T> async(Supplier<T> supplier) {
        return async(supplier, ForkJoinPool.commonPool());
    }

    public static <T> @NotNull DefaultPromise<T> async(Supplier<T> supplier, Executor executor) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, executor);
        return new DefaultPromise<>(future);
    }

    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull DefaultPromise<T> fromCompletableFuture(CompletableFuture<T> future) {
        return new DefaultPromise<>(future);
    }

    @Contract("_ -> new")
    public static @NotNull DefaultPromise<Void> sleep(Duration duration) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        PromiseCompletableFuture.getInstance().schedule(
                () -> future.complete(null),
                duration
        );
        return new DefaultPromise<>(future);
    }

    // ==================== Transformations ====================

    @Override
    public <U> Promise<U> map(Function<? super T, ? extends U> mapper) {
        CompletableFuture<U> mapped = future.thenApply(mapper);
        return new DefaultPromise<>(mapped);
    }

    @Override
    public <U> Promise<U> flatMap(Function<? super T, ? extends Promise<U>> mapper) {
        CompletableFuture<U> flattened = future.thenCompose(value -> {
            Promise<U> nextPromise = mapper.apply(value);
            return nextPromise.toCompletableFuture();
        });
        return new DefaultPromise<>(flattened);
    }

    @Override
    public Promise<T> tap(Consumer<? super T> consumer) {
        CompletableFuture<T> tapped = future.whenComplete((value, error) -> {
            if (error == null) {
                consumer.accept(value);
            }
        });
        return new DefaultPromise<>(tapped);
    }

    @Override
    public Promise<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, () -> new IllegalStateException("Filter predicate failed"));
    }

    @Override
    public Promise<T> filter(Predicate<? super T> predicate, Supplier<Throwable> errorSupplier) {
        return map(value -> {
            if (predicate.test(value)) {
                return value;
            } else {
                throw new RuntimeException(errorSupplier.get());
            }
        });
    }

    // ==================== Error Handling ====================

    @Override
    public Promise<T> recover(Function<Throwable, ? extends T> recoveryFunction) {
        CompletableFuture<T> recovered = future.exceptionally(recoveryFunction);
        return new DefaultPromise<>(recovered);
    }

    @Override
    public Promise<T> recoverWith(Function<Throwable, ? extends Promise<T>> recoveryFunction) {
        CompletableFuture<T> recovered = future.handle((value, error) -> {
            if (error != null) {
                return recoveryFunction.apply(unwrapException(error)).toCompletableFuture();
            } else {
                return CompletableFuture.completedFuture(value);
            }
        }).thenCompose(Function.identity());
        return new DefaultPromise<>(recovered);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Throwable> Promise<T> catchError(Class<E> errorType, Function<E, ? extends T> handler) {
        return recover(error -> {
            if (errorType.isInstance(error)) {
                return handler.apply((E) error);
            } else {
                throw new RuntimeException(error);
            }
        });
    }

    @Override
    public Promise<T> catchError(Function<Throwable, ? extends T> handler) {
        return recover(handler);
    }

    @Override
    public Promise<T> mapError(Function<Throwable, Throwable> errorMapper) {
        CompletableFuture<T> mapped = future.handle((value, error) -> {
            if (error != null) {
                throw new RuntimeException(errorMapper.apply(unwrapException(error)));
            }
            return value;
        }).thenCompose(CompletableFuture::completedFuture);
        return new DefaultPromise<>(mapped);
    }

    @Override
    public Promise<T> finallyDo(Runnable action) {
        CompletableFuture<T> withFinally = future.whenComplete((value, error) -> action.run());
        return new DefaultPromise<>(withFinally);
    }

    // ==================== Timing & Control ====================

    @Override
    public Promise<T> timeout(Duration duration) {
        CompletableFuture<T> withTimeout = future.orTimeout(duration.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(error -> {
                    if (error instanceof java.util.concurrent.TimeoutException) {
                        throw new RuntimeException(new TimeoutException(duration));
                    }
                    throw new RuntimeException(error);
                });
        return new DefaultPromise<>(withTimeout);
    }

    @Override
    public Promise<T> delay(Duration duration) {
        return flatMap(value ->
                DefaultPromise.sleep(duration).map(v -> value)
        );
    }

    @Override
    public Promise<T> retry(RetryPolicy policy) {
        return new RetryHandler<>(this, policy).execute();
    }

    // ==================== State & Cancellation ====================

    @Override
    public PromiseState state() {
        if (future.isDone()) {
            if (future.isCancelled()) {
                return PromiseState.CANCELLED;
            }
            try {
                future.getNow(null);
                return PromiseState.FULFILLED;
            } catch (CompletionException e) {
                return PromiseState.REJECTED;
            }
        }
        return PromiseState.PENDING;
    }

    @Override
    public boolean cancel() {
        return future.cancel(true);
    }

    @Override
    public boolean cancel(String reason) {
        return future.completeExceptionally(new io.obsidian.promise.error.CancellationException(reason));
    }

    // ==================== Callbacks ====================

    @Override
    public Promise<T> onSuccess(Consumer<? super T> onSuccess) {
        future.thenAccept(onSuccess);
        return this;
    }

    @Override
    public Promise<T> onError(Consumer<Throwable> onError) {
        future.exceptionally(error -> {
            onError.accept(unwrapException(error));
            return null;
        });
        return this;
    }

    @Override
    public Promise<T> onCancelled(Runnable onCancelled) {
        future.exceptionally(error -> {
            if (error instanceof CancellationException ||
                    error instanceof io.obsidian.promise.error.CancellationException) {
                onCancelled.run();
            }
            return null;
        });
        return this;
    }

    @Override
    public Promise<T> onComplete(Runnable onComplete) {
        future.whenComplete((value, error) -> onComplete.run());
        return this;
    }

    // ==================== Blocking Operations ====================

    @Override
    public T get() throws Throwable {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw unwrapException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public T get(Duration timeout) throws Throwable {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw unwrapException(e);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new TimeoutException(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public T getOrDefault(T defaultValue) {
        try {
            return future.getNow(defaultValue);
        } catch (CompletionException e) {
            return defaultValue;
        }
    }

    @Override
    public T getOrElse(Supplier<? extends T> defaultSupplier) {
        try {
            return future.getNow(null);
        } catch (CompletionException e) {
            return defaultSupplier.get();
        }
    }

    // ==================== Interop ====================

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }

    // ==================== Helpers ====================

    private static Throwable unwrapException(Throwable error) {
        if (error instanceof CompletionException && error.getCause() != null) {
            return error.getCause();
        }
        if (error instanceof ExecutionException && error.getCause() != null) {
            return error.getCause();
        }
        return error;
    }

    /**
     * Internal retry handler.
     */
    private static class RetryHandler<T> {
        private final Promise<T> promise;
        private final RetryPolicy policy;

        RetryHandler(Promise<T> promise, RetryPolicy policy) {
            this.promise = promise;
            this.policy = policy;
        }

        Promise<T> execute() {
            return attemptWithRetry(1);
        }

        private Promise<T> attemptWithRetry(int attemptNumber) {
            return promise.recoverWith(error -> {
                if (attemptNumber > policy.maxAttempts() || !policy.shouldRetry(error)) {
                    return Promises.error(error);
                }

                Duration backoff = policy.backoff(attemptNumber, error);

                if (backoff.isZero()) {
                    return attemptWithRetry(attemptNumber + 1);
                } else {
                    return DefaultPromise.sleep(backoff)
                            .flatMap(v -> attemptWithRetry(attemptNumber + 1));
                }
            });
        }
    }
}