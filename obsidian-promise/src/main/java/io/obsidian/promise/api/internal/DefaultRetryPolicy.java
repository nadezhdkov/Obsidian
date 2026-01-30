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

package io.obsidian.promise.api.internal;

import io.obsidian.promise.api.BackoffStrategy;
import io.obsidian.promise.api.RetryPolicy;

import java.time.Duration;
import java.util.function.Predicate;

public class DefaultRetryPolicy implements RetryPolicy {

    private final int                  maxAttempts;
    private final BackoffStrategy      backoffStrategy;
    private final Predicate<Throwable> retryPredicate;

    DefaultRetryPolicy(int maxAttempts, BackoffStrategy backoffStrategy, Predicate<Throwable> retryPredicate) {
        this.maxAttempts     = maxAttempts;
        this.backoffStrategy = backoffStrategy;
        this.retryPredicate  = retryPredicate;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public Duration backoff(int attempt, Throwable lastError) {
        return backoffStrategy.calculateDelay(attempt);
    }

    @Override
    public boolean shouldRetry(Throwable error) {
        return retryPredicate.test(error);
    }

}
