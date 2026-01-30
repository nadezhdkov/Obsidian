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

/**
 * A Deferred represents the producer side of a Promise.
 *
 * <p>It allows manual control over a Promise's completion:
 * <ul>
 *   <li>resolve - complete successfully with a value</li>
 *   <li>reject - complete with an error</li>
 *   <li>cancel - cancel the operation</li>
 * </ul>
 *
 * <p>The associated Promise (obtained via {@link #promise()}) represents the consumer side
 * and can be passed to clients who should only observe the result.
 *
 * <p>Example usage:
 * <pre>{@code
 * Deferred<String> deferred = Promises.defer();
 *
 * // Pass the promise to consumers
 * Promise<String> promise = deferred.promise();
 *
 * // Later, complete it
 * if (success) {
 *     deferred.resolve("result");
 * } else {
 *     deferred.reject(new Exception("failed"));
 * }
 * }</pre>
 *
 * @param <T> the type of value this Deferred will produce
 */
public interface Deferred<T> {

    /**
     * Returns the Promise associated with this Deferred.
     *
     * <p>The Promise will complete when this Deferred is resolved, rejected, or cancelled.
     *
     * @return the associated Promise
     */
    Promise<T> promise();

    /**
     * Completes the Promise successfully with the given value.
     *
     * <p>If the Promise is already completed, this method has no effect.
     *
     * @param value the value to complete with
     * @return true if the Promise was completed by this call, false if already completed
     */
    boolean resolve(T value);

    /**
     * Completes the Promise with an error.
     *
     * <p>If the Promise is already completed, this method has no effect.
     *
     * @param error the error to reject with
     * @return true if the Promise was rejected by this call, false if already completed
     */
    boolean reject(Throwable error);

    /**
     * Cancels the Promise.
     *
     * <p>If the Promise is already completed, this method has no effect.
     *
     * @return true if the Promise was cancelled by this call, false if already completed
     */
    boolean cancel();

    /**
     * Cancels the Promise with a specific reason.
     *
     * @param reason the reason for cancellation
     * @return true if the Promise was cancelled by this call, false if already completed
     */
    boolean cancel(String reason);

    /**
     * Returns true if this Deferred's Promise has been completed
     * (resolved, rejected, or cancelled).
     */
    boolean isCompleted();

    /**
     * Attempts to complete this Deferred with the result of another Promise.
     *
     * <p>When the given Promise completes:
     * <ul>
     *   <li>If it succeeds, this Deferred is resolved with the same value</li>
     *   <li>If it fails, this Deferred is rejected with the same error</li>
     *   <li>If it's cancelled, this Deferred is cancelled</li>
     * </ul>
     *
     * @param promise the Promise to link to
     */
    void completeWith(Promise<? extends T> promise);
}