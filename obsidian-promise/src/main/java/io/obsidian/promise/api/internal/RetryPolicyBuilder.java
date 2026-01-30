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

public class RetryPolicyBuilder implements RetryPolicy.RetryPolicyConfigurer {

    private int                 maxAttempts     = 3;
    private BackoffStrategy     backoffStrategy = BackoffStrategy.none();
    private Predicate<Throwable> retryPredicate = e -> true;

    @Override
    public RetryPolicy.RetryPolicyConfigurer maxAttempts(int maxAttempts) {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must be non-negative");
        }
        this.maxAttempts = maxAttempts;
        return this;
    }

    @Override
    public RetryPolicy.RetryPolicyConfigurer fixedDelay(Duration delay) {
        this.backoffStrategy = BackoffStrategy.fixed(delay);
        return this;
    }

    @Override
    public RetryPolicy.RetryPolicyConfigurer exponentialBackoff(Duration initialDelay) {
        this.backoffStrategy = BackoffStrategy.exponential(initialDelay, 2.0);
        return this;
    }

    @Override
    public RetryPolicy.RetryPolicyConfigurer exponentialBackoff(Duration initialDelay, double multiplier) {
        this.backoffStrategy = BackoffStrategy.exponential(initialDelay, multiplier);
        return this;
    }

    @Override
    public RetryPolicy.RetryPolicyConfigurer maxDelay(Duration maxDelay) {
        this.backoffStrategy = this.backoffStrategy.withMaxDelay(maxDelay);
        return this;
    }

    @Override
    public RetryPolicy.RetryPolicyConfigurer withJitter() {
        this.backoffStrategy = this.backoffStrategy.withJitter();
        return this;
    }

    @Override
    public RetryPolicy.RetryPolicyConfigurer retryIf(Predicate<Throwable> predicate) {
        this.retryPredicate = predicate;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RetryPolicy.RetryPolicyConfigurer retryOn(Class<? extends Throwable>... errorTypes) {
        this.retryPredicate = error -> {
            for (Class<? extends Throwable> type : errorTypes) {
                if (type.isInstance(error)) {
                    return true;
                }
            }
            return false;
        };
        return this;
    }

    @Override
    public RetryPolicy build() {
        return new DefaultRetryPolicy(maxAttempts, backoffStrategy, retryPredicate);
    }
}