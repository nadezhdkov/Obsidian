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

import io.obsidian.promise.api.Deferred;
import io.obsidian.promise.api.Promise;

import java.util.concurrent.CompletableFuture;

public class DefaultDeferred<T> implements Deferred<T> {

    private final CompletableFuture<T> future;
    private final DefaultPromise<T> promise;

    public DefaultDeferred() {
        this.future = new CompletableFuture<>();
        this.promise = DefaultPromise.fromCompletableFuture(future);
    }

    @Override
    public Promise<T> promise() {
        return promise;
    }

    @Override
    public boolean resolve(T value) {
        return future.complete(value);
    }

    @Override
    public boolean reject(Throwable error) {
        return future.completeExceptionally(error);
    }

    @Override
    public boolean cancel() {
        return future.cancel(true);
    }

    @Override
    public boolean cancel(String reason) {
        return future.completeExceptionally(new io.obsidian.promise.error.CancellationException(reason));
    }

    @Override
    public boolean isCompleted() {
        return future.isDone();
    }

    @Override
    public void completeWith(Promise<? extends T> otherPromise) {
        otherPromise
                .onSuccess(this::resolve)
                .onError(this::reject)
                .onCancelled(this::cancel);
    }
}