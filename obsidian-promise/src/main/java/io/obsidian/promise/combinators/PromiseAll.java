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

package io.obsidian.promise.combinators;

import io.obsidian.promise.api.Deferred;
import io.obsidian.promise.api.Promise;
import io.obsidian.promise.api.Promises;
import io.obsidian.promise.error.AggregateException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Combines multiple promises into one that resolves when all input promises succeed.
 *
 * <p>Behavior:
 * <ul>
 *   <li>If all promises succeed, resolves with a list of results in order</li>
 *   <li>If any promise fails, rejects immediately with that error (fail-fast)</li>
 *   <li>If any promise is cancelled, cancels the combined promise</li>
 * </ul>
 */
public class PromiseAll {

    private PromiseAll() {}

    /**
     * Combines the given promises into one that resolves when all succeed.
     *
     * @param promises the promises to combine
     * @return a promise that resolves to a list of results
     */
    public static <T> Promise<List<T>> of(List<Promise<? extends T>> promises) {
        if (promises == null || promises.isEmpty()) {
            return Promises.value(new ArrayList<>());
        }

        if (promises.size() == 1) {
            return promises.getFirst().map(result -> {
                List<T> list = new ArrayList<>();
                list.add(result);
                return list;
            });
        }

        return new PromiseCollector<>(promises).execute();
    }

    /**
     * Implementation of a mechanism to combine multiple promises into a single promise
     * that resolves when all supplied promises are resolved or rejects if any promise fails.
     *
     * @param <T> the type of the result produced by the promises
     */
    private static class PromiseCollector<T> {
        private final List<Promise<? extends T>> promises;
        private final Deferred<List<T>> deferred;
        private final AtomicInteger remaining;
        private final AtomicReference<List<T>> results;
        private final AtomicReference<Throwable> firstError;

        PromiseCollector(@NotNull List<Promise<? extends T>> promises) {
            this.promises   = promises;
            this.deferred   = Promises.defer();
            this.remaining  = new AtomicInteger(promises.size());
            this.results    = new AtomicReference<>(createResultsList(promises.size()));
            this.firstError = new AtomicReference<>();
        }

        Promise<List<T>> execute() {
            for (int i = 0; i < promises.size(); i++) {
                final int index = i;
                Promise<? extends T> promise = promises.get(i);

                promise
                        .onSuccess(result -> onSuccess(index, result))
                        .onError(this::onError)
                        .onCancelled(this::onCancelled);
            }

            return deferred.promise();
        }

        private void onSuccess(int index, T result) {
            results.get().set(index, result);

            if (remaining.decrementAndGet() == 0) {
                deferred.resolve(results.get());
            }
        }

        private void onError(Throwable error) {
            if (firstError.compareAndSet(null, error)) {
                deferred.reject(error);

                cancelRemaining();
            }
        }

        private void onCancelled() {
            if (firstError.compareAndSet(null, new io.obsidian.promise.error.CancellationException())) {
                deferred.cancel("One of the promises was cancelled");
                cancelRemaining();
            }
        }

        private void cancelRemaining() {
            for (Promise<? extends T> promise : promises) {
                if (promise.isPending()) {
                    promise.cancel();
                }
            }
        }

        private static <T> @NotNull List<T> createResultsList(int size) {
            List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(null);
            }
            return list;
        }
    }

    /**
     * Combines promises but collects all errors instead of failing fast.
     *
     * <p>If any promises fail, rejects with an AggregateException containing all errors.
     * Otherwise, resolves with all successful results.
     */
    public static <T> Promise<List<T>> aggregateResults(List<Promise<? extends T>> promises) {
        if (promises == null || promises.isEmpty()) {
            return Promises.value(new ArrayList<>());
        }

        return new PromiseAggregator<>(promises).execute();
    }

    private static class PromiseAggregator<T> {
        private final List<Promise<? extends T>> promises;
        private final Deferred<List<T>>          deferred;
        private final AtomicInteger              remaining;
        private final List<T>                    results;
        private final List<Throwable>            errors;

        PromiseAggregator(@NotNull List<Promise<? extends T>> promises) {
            this.promises  = promises;
            this.deferred  = Promises.defer();
            this.remaining = new AtomicInteger(promises.size());
            this.results   = createResultsList(promises.size());
            this.errors    = new ArrayList<>();
        }

        Promise<List<T>> execute() {
            for (int i = 0; i < promises.size(); i++) {
                Promise<? extends T> promise = getPromise(i);

                promise.onCancelled(() -> {
                    synchronized (this) {
                        errors.add(new io.obsidian.promise.error.CancellationException());
                    }
                    checkCompletion();
                });
            }

            return deferred.promise();
        }

        private @NotNull Promise<? extends T> getPromise(int i) {
            final int index = i;
            Promise<? extends T> promise = promises.get(i);

            promise.onSuccess(result -> {
                synchronized (this) {
                    results.set(index, result);
                }
                checkCompletion();
            });

            promise.onError(error -> {
                synchronized (this) {
                    errors.add(error);
                }
                checkCompletion();
            });
            return promise;
        }

        private void checkCompletion() {
            if (remaining.decrementAndGet() == 0) {
                synchronized (this) {
                    if (errors.isEmpty()) {
                        deferred.resolve(results);
                    } else {
                        deferred.reject(new AggregateException(errors));
                    }
                }
            }
        }

        private static <T> @NotNull List<T> createResultsList(int size) {
            List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(null);
            }
            return list;
        }
    }
}