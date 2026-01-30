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

package io.obsidian.promise.api.internal.backoff;

import io.obsidian.promise.api.BackoffStrategy;

import java.time.Duration;

public class JitteredBackoff implements BackoffStrategy {
    private final BackoffStrategy delegate;

    public JitteredBackoff(BackoffStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public Duration calculateDelay(int attempt) {
        Duration baseDelay = delegate.calculateDelay(attempt);
        long millis        = baseDelay.toMillis();
        long jittered      = (long) (millis * (0.5 + Math.random() * 0.5));
        return Duration.ofMillis(jittered);
    }

    @Override
    public BackoffStrategy withMaxDelay(Duration maxDelay) {
        return new JitteredBackoff(delegate.withMaxDelay(maxDelay));
    }

    @Override
    public BackoffStrategy withJitter() {
        return this;
    }
}