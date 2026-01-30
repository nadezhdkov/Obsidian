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

import io.obsidian.promise.api.internal.cancellation.NoCancellationToken;
import io.obsidian.promise.api.internal.cancellation.PreCancelledToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A token that can be used to signal cancellation of an asynchronous operation.
 *
 * <p>CancellationToken is a read-only view of cancellation state. To create and control
 * a token, use {@link CancellationSource}.
 *
 * <p>Example usage:
 * <pre>{@code
 * void performWork(CancellationToken token) {
 *     while (!token.isCancelled()) {
 *         // Check periodically
 *         token.throwIfCancelled();
 *         // Do work...
 *     }
 * }
 * }</pre>
 */
public interface CancellationToken {

    /**
     * Returns true if cancellation has been requested.
     */
    boolean isCancelled();

    /**
     * Returns the reason for cancellation, if any.
     */
    String reason();

    /**
     * Throws CancellationException if cancellation has been requested.
     *
     * @throws io.obsidian.promise.error.CancellationException if cancelled
     */
    void throwIfCancelled();

    /**
     * Registers a callback to be invoked when cancellation is requested.
     *
     * <p>If cancellation has already been requested, the callback is invoked immediately.
     *
     * @param callback the callback to invoke on cancellation
     */
    void onCancelled(Runnable callback);

    /**
     * Returns a token that is never cancelled.
     */
    static CancellationToken none() {
        return NoCancellationToken.getInstance();
    }

    /**
     * Returns a token that is already cancelled.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull CancellationToken cancelled() {
        return cancelled("Already cancelled");
    }

    /**
     * Returns a token that is already cancelled with a reason.
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull CancellationToken cancelled(String reason) {
        return new PreCancelledToken(reason);
    }
}
