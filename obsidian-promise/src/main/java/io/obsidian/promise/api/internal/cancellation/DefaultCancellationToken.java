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

package io.obsidian.promise.api.internal.cancellation;

import io.obsidian.promise.api.CancellationToken;
import io.obsidian.promise.error.CancellationException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultCancellationToken implements CancellationToken {

    private final AtomicBoolean            cancelled = new AtomicBoolean(false);
    private final AtomicReference<String>  reason    = new AtomicReference<>();
    private final java.util.List<Runnable> callbacks = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public String reason() {
        return reason.get();
    }

    @Override
    public void throwIfCancelled() {
        if (cancelled.get()) {
            throw new CancellationException(reason.get());
        }
    }

    @Override
    public void onCancelled(Runnable callback) {
        if (cancelled.get()) {
            callback.run();
            return;
        }

        callbacks.add(callback);

        if (cancelled.get()) {
            callback.run();
        }
    }

    void cancel(String cancelReason) {
        if (cancelled.compareAndSet(false, true)) {
            reason.set(cancelReason);
            callbacks.forEach(Runnable::run);
            callbacks.clear();
        }
    }
}