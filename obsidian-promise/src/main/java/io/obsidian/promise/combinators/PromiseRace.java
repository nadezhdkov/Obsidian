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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Returns the first promise that completes (success or failure).
 *
 * <p>Unlike {@link PromiseAny}, this resolves with the first completion
 * regardless of whether it's a success or failure.
 */
public class PromiseRace {

    private PromiseRace() {}

    /**
     * Returns a promise that completes with the first result (success or failure).
     *
     * @param promises the promises to race
     * @return a promise that completes with the first result
     */
    public static <T> Promise<T> of(List<Promise<? extends T>> promises) {
        if (promises == null || promises.isEmpty()) {
            return Promises.error(new IllegalArgumentException("At least one promise is required"));
        }

        if (promises.size() == 1) {
            @SuppressWarnings("unchecked")
            Promise<T> single = (Promise<T>) promises.getFirst();
            return single;
        }

        return new PromiseAggregator<>(promises).execute();
    }

    private static class PromiseAggregator<T> {
        private final List<Promise<? extends T>> promises;
        private final Deferred<T> deferred;
        private final AtomicBoolean completed;

        PromiseAggregator(List<Promise<? extends T>> promises) {
            this.promises = promises;
            this.deferred = Promises.defer();
            this.completed = new AtomicBoolean(false);
        }

        Promise<T> execute() {
            for (Promise<? extends T> promise : promises) {
                promise
                        .onSuccess(this::onSuccess)
                        .onError(this::onError)
                        .onCancelled(this::onCancelled);
            }

            return deferred.promise();
        }

        private void onSuccess(T result) {
            if (completed.compareAndSet(false, true)) {
                deferred.resolve(result);
                cancelRemaining();
            }
        }

        private void onError(Throwable error) {
            if (completed.compareAndSet(false, true)) {
                deferred.reject(error);
                cancelRemaining();
            }
        }

        private void onCancelled() {
            if (completed.compareAndSet(false, true)) {
                deferred.cancel("First promise was cancelled");
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
    }
}