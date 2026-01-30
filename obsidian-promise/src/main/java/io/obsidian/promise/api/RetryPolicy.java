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

import io.obsidian.promise.api.internal.RetryPolicyBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Defines a policy for retrying failed operations.
 *
 * <p>A RetryPolicy specifies:
 * <ul>
 *   <li>How many times to retry</li>
 *   <li>How long to wait between retries (backoff strategy)</li>
 *   <li>Which errors should trigger a retry</li>
 * </ul>
 */
public interface RetryPolicy {

    /**
     * Returns the maximum number of retry attempts.
     *
     * @return max attempts (not including the initial attempt)
     */
    int maxAttempts();

    /**
     * Calculates the delay before the next retry attempt.
     *
     * @param attempt the attempt number (1-based)
     * @param lastError the error from the previous attempt
     * @return the duration to wait before retrying
     */
    Duration backoff(int attempt, Throwable lastError);

    /**
     * Determines if the given error should trigger a retry.
     *
     * @param error the error that occurred
     * @return true if should retry, false otherwise
     */
    boolean shouldRetry(Throwable error);

    /**
     * Creates a new builder for constructing RetryPolicy instances.
     */
    @Contract(" -> new")
    static @NotNull RetryPolicy.RetryPolicyConfigurer configure() {
        return new RetryPolicyBuilder();
    }

    /**
     * Creates a simple retry policy with fixed attempts and no delay.
     */
    static RetryPolicy simple(int maxAttempts) {
        return configure().maxAttempts(maxAttempts).build();
    }

    /**
     * Creates a retry policy with exponential backoff.
     */
    static RetryPolicy exponential(int maxAttempts, Duration initialDelay) {
        return configure()
                .maxAttempts(maxAttempts)
                .exponentialBackoff(initialDelay)
                .build();
    }

    /**
     * Builder for creating RetryPolicy instances.
     */
    interface RetryPolicyConfigurer {
        /**
         * Sets the maximum number of retry attempts.
         */
        RetryPolicyConfigurer maxAttempts(int maxAttempts);

        /**
         * Sets a fixed delay between retries.
         */
        RetryPolicyConfigurer fixedDelay(Duration delay);

        /**
         * Sets exponential backoff with the given initial delay.
         * Each retry doubles the delay time.
         */
        RetryPolicyConfigurer exponentialBackoff(Duration initialDelay);

        /**
         * Sets exponential backoff with multiplier.
         */
        RetryPolicyConfigurer exponentialBackoff(Duration initialDelay, double multiplier);

        /**
         * Sets the maximum delay for exponential backoff.
         */
        RetryPolicyConfigurer maxDelay(Duration maxDelay);

        /**
         * Adds jitter to backoff delays to avoid thundering herd.
         */
        RetryPolicyConfigurer withJitter();

        /**
         * Sets a predicate to determine which errors should trigger retry.
         */
        RetryPolicyConfigurer retryIf(Predicate<Throwable> predicate);

        /**
         * Only retry on specific exception types.
         */
        @SuppressWarnings("unchecked")
        RetryPolicyConfigurer retryOn(Class<? extends Throwable>... errorTypes);

        /**
         * Builds the RetryPolicy.
         */
        RetryPolicy build();
    }
}