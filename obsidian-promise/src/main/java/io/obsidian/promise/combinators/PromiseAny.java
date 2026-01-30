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
import io.obsidian.promise.error.CancellationException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Returns the first promise that succeeds.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Resolves with the value of the first successful promise</li>
 *   <li>If all promises fail, rejects with an AggregateException</li>
 *   <li>Ignores failures until all promises have failed</li>
 * </ul>
 */
public class PromiseAny {

    private PromiseAny() {}

    /**
     * Returns a promise that resolves with the first successful result.
     *
     * @param promises the promises to check
     * @return a promise that resolves with the first success
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

        return new PromiseAnyImpl<>(promises).execute();
    }

    private static class PromiseAnyImpl<T> {
        private final List<Promise<? extends T>> promises;
        private final Deferred<T>                deferred;
        private final AtomicBoolean              resolved;
        private final AtomicInteger              failedCount;
        private final List<Throwable>            errors;

        PromiseAnyImpl(List<Promise<? extends T>> promises) {
            this.promises    = promises;
            this.deferred    = Promises.defer();
            this.resolved    = new AtomicBoolean(false);
            this.failedCount = new AtomicInteger(0);
            this.errors      = new ArrayList<>();
        }

        Promise<T> execute() {
            for (Promise<? extends T> promise : promises) {
                promise.onSuccess   (this::onSuccess)
                        .onError    (this::onError)
                        .onCancelled(this::onCancelled);
            }

            return deferred.promise();
        }

        private void onSuccess(T result) {
            if (resolved.compareAndSet(false, true)) {
                deferred.resolve(result);

                cancelRemaining();
            }
        }

        private void onError(Throwable error) {
            synchronized (errors) {
                errors.add(error);
            }

            if (failedCount.incrementAndGet() == promises.size()) {
                if (resolved.compareAndSet(false, true)) {
                    synchronized (errors) {
                        deferred.reject(new AggregateException("All promises failed", errors));
                    }
                }
            }
        }

        private void onCancelled() {
            onError(new CancellationException());
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