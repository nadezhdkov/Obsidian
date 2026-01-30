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

package io.obsidian.promise.api;

import io.obsidian.promise.internal.impl.DefaultDeferred;
import io.obsidian.promise.internal.impl.DefaultPromise;
import io.obsidian.promise.combinators.PromiseAll;
import io.obsidian.promise.combinators.PromiseAny;
import io.obsidian.promise.combinators.PromiseRace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Utility class for creating and working with Promises.
 *
 * <p>This is the main entry point for the Promise API. It provides factory methods
 * for creating promises in various ways.
 *
 * <p>Examples:
 * <pre>{@code
 * // Create a resolved promise
 * Promise<String> p1 = Promises.value("hello");
 *
 * // Create from async computation
 * Promise<Data> p2 = Promises.async(() -> loadData());
 *
 * // Create from CompletableFuture
 * CompletableFuture<String> cf = ...;
 * Promise<String> p3 = Promises.from(cf);
 *
 * // Combine multiple promises
 * Promise<List<String>> all = Promises.all(p1, p2, p3);
 * }</pre>
 */
public final class Promises {

    private Promises() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== Simple Creation ====================

    /**
     * Creates a promise that is already resolved with the given value.
     *
     * @param value the value to resolve with
     * @return a resolved promise
     */
    public static <T> Promise<T> value(T value) {
        return DefaultPromise.resolved(value);
    }

    /**
     * Creates a promise that is already rejected with the given error.
     *
     * @param error the error to reject with
     * @return a rejected promise
     */
    public static <T> Promise<T> error(Throwable error) {
        return DefaultPromise.rejected(error);
    }

    /**
     * Creates a promise that is already cancelled.
     *
     * @return a cancelled promise
     */
    public static <T> Promise<T> cancelled() {
        return DefaultPromise.cancelled();
    }

    /**
     * Creates a promise that is already cancelled with a reason.
     */
    public static <T> Promise<T> cancelled(String reason) {
        return DefaultPromise.cancelled(reason);
    }

    // ==================== Async Creation ====================

    /**
     * Creates a promise from a synchronous supplier.
     * The supplier is executed asynchronously on the default executor.
     *
     * @param supplier the supplier to execute
     * @return a promise representing the computation
     */
    public static <T> Promise<T> async(Supplier<T> supplier) {
        return DefaultPromise.async(supplier);
    }

    /**
     * Creates a promise from a supplier, executed on the given executor.
     *
     * @param supplier the supplier to execute
     * @param executor the executor to run on
     * @return a promise representing the computation
     */
    public static <T> @NotNull Promise<T> async(Supplier<T> supplier, Executor executor) {
        return DefaultPromise.async(supplier, executor);
    }

    /**
     * Creates a promise from a callable.
     *
     * @param callable the callable to execute
     * @return a promise representing the computation
     */
    public static <T> @NotNull Promise<T> call(Callable<T> callable) {
        return async(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Creates a promise from a runnable.
     * The promise resolves to null when the runnable completes.
     *
     * @param runnable the runnable to execute
     * @return a promise that completes when the runnable finishes
     */
    public static @NotNull Promise<Void> run(Runnable runnable) {
        return async(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Creates a promise from a runnable on the given executor.
     */
    public static @NotNull Promise<Void> run(Runnable runnable, Executor executor) {
        return async(() -> {
            runnable.run();
            return null;
        }, executor);
    }

    // ==================== Deferred ====================

    /**
     * Creates a new Deferred, allowing manual control over promise completion.
     *
     * <p>Use this when you need to complete a promise from external events
     * or callbacks that don't fit the async supplier pattern.
     *
     * @return a new Deferred
     */
    @Contract(" -> new")
    public static <T> @NotNull Deferred<T> defer() {
        return new DefaultDeferred<>();
    }

    // ==================== CompletableFuture Bridge ====================

    /**
     * Converts a CompletableFuture to a Promise.
     *
     * @param future the CompletableFuture to convert
     * @return a Promise wrapping the future
     */
    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull Promise<T> from(CompletableFuture<T> future) {
        return DefaultPromise.fromCompletableFuture(future);
    }

    /**
     * Converts a standard Future to a Promise.
     * The future is polled on the default executor.
     */
    public static <T> @NotNull Promise<T> from(java.util.concurrent.Future<T> future) {
        return async(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== Combinators ====================

    /**
     * Combines multiple promises into one that resolves when all succeed.
     *
     * <p>If any promise fails, the combined promise fails immediately (fail-fast).
     * If all promises succeed, returns a list of results in the same order.
     *
     * @param promises the promises to combine
     * @return a promise that resolves to a list of all results
     */
    @SafeVarargs
    public static <T> Promise<List<T>> all(Promise<? extends T>... promises) {
        return PromiseAll.of(Arrays.asList(promises));
    }

    /**
     * Combines a list of promises into one that resolves when all succeed.
     */
    public static <T> Promise<List<T>> all(List<Promise<? extends T>> promises) {
        return PromiseAll.of(promises);
    }

    /**
     * Returns the first promise that succeeds.
     *
     * <p>If all promises fail, returns an AggregateException containing all errors.
     *
     * @param promises the promises to race
     * @return a promise that resolves with the first success
     */
    @SafeVarargs
    public static <T> Promise<T> any(Promise<? extends T>... promises) {
        return PromiseAny.of(Arrays.asList(promises));
    }

    /**
     * Returns the first promise that succeeds from a list.
     */
    public static <T> Promise<T> any(List<Promise<? extends T>> promises) {
        return PromiseAny.of(promises);
    }

    /**
     * Returns the first promise that completes (success or failure).
     *
     * @param promises the promises to race
     * @return a promise that completes with the first result
     */
    @SafeVarargs
    public static <T> Promise<T> race(Promise<? extends T>... promises) {
        return PromiseRace.of(Arrays.asList(promises));
    }

    /**
     * Returns the first promise that completes from a list.
     */
    public static <T> Promise<T> race(List<Promise<? extends T>> promises) {
        return PromiseRace.of(promises);
    }

    // ==================== Timing ====================

    /**
     * Creates a promise that resolves after a delay.
     *
     * @param duration the delay duration
     * @return a promise that completes after the delay
     */
    @Contract("_ -> new")
    public static @NotNull Promise<Void> delay(Duration duration) {
        return DefaultPromise.sleep(duration);
    }

    /**
     * Creates a promise that resolves with a value after a delay.
     */
    public static <T> Promise<T> delay(T value, Duration duration) {
        return delay(duration).map(v -> value);
    }

    // ==================== Utilities ====================

    /**
     * Wraps a potentially throwing operation in a Promise.
     *
     * <p>If the supplier throws, the promise is rejected with that error.
     * Otherwise, it's resolved with the value.
     *
     * @param supplier the operation to wrap
     * @return a promise representing the operation
     */
    public static <T> Promise<T> wrap(Supplier<T> supplier) {
        try {
            return value(supplier.get());
        } catch (Throwable t) {
            return error(t);
        }
    }

    /**
     * Creates a promise that never completes.
     * Useful for testing timeouts.
     */
    @SuppressWarnings("unchecked")
    public static <T> Promise<T> never() {
        return (Promise<T>) defer().promise();
    }
}