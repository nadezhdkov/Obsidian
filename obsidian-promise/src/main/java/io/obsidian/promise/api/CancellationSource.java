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

import io.obsidian.promise.api.internal.cancellation.DefaultCancellationSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Controls a CancellationToken, allowing cancellation to be signaled.
 *
 * <p>A CancellationSource creates a CancellationToken that can be passed to operations
 * that should be cancellable. The source can then signal cancellation at any time.
 *
 * <p>Example:
 * <pre>{@code
 * CancellationSource source = CancellationSource.create();
 *
 * Promise<Data> promise = Promises.async(() -> {
 *     return loadData(source.token());
 * });
 *
 * // Later, cancel the operation
 * source.cancel();
 * }</pre>
 */
public interface CancellationSource {

    /**
     * Returns the CancellationToken controlled by this source.
     */
    CancellationToken token();

    /**
     * Signals cancellation on the associated token.
     */
    void cancel();

    /**
     * Signals cancellation with a specific reason.
     *
     * @param reason the reason for cancellation
     */
    void cancel(String reason);

    /**
     * Returns true if cancellation has been signaled.
     */
    boolean isCancelled();

    /**
     * Creates a new CancellationSource.
     */
    @Contract(" -> new")
    static @NotNull CancellationSource create() {
        return new DefaultCancellationSource();
    }

    /**
     * Creates a linked CancellationSource that will be cancelled when any of the
     * parent tokens are cancelled.
     *
     * @param tokens the parent tokens to link to
     * @return a new CancellationSource linked to the parent tokens
     */
    static @NotNull CancellationSource createLinked(CancellationToken @NotNull ... tokens) {
        DefaultCancellationSource source = new DefaultCancellationSource();

        for (CancellationToken token : tokens) {
            token.onCancelled(() -> source.cancel("Linked cancellation"));
        }

        return source;
    }
}
