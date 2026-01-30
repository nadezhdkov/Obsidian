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

public class ExponentialBackoff implements BackoffStrategy {

    private final Duration  initialDelay;
    private final double    multiplier;
    private final Duration  maxDelay;
    private final boolean   useJitter;

    public ExponentialBackoff(Duration initialDelay, double multiplier, Duration maxDelay, boolean useJitter) {
        this.initialDelay = initialDelay;
        this.multiplier   = multiplier;
        this.maxDelay     = maxDelay;
        this.useJitter    = useJitter;
    }

    @Override
    public Duration calculateDelay(int attempt) {
        long millis = initialDelay.toMillis();
        long delay  = (long) (millis * Math.pow(multiplier, attempt - 1));

        if (maxDelay != null && delay > maxDelay.toMillis()) {
            delay = maxDelay.toMillis();
        }

        if (useJitter) {
            delay = (long) (delay * (0.5 + Math.random() * 0.5));
        }

        return Duration.ofMillis(delay);
    }

    @Override
    public BackoffStrategy withMaxDelay(Duration maxDelay) {
        return new ExponentialBackoff(initialDelay, multiplier, maxDelay, useJitter);
    }

    @Override
    public BackoffStrategy withJitter() {
        return new ExponentialBackoff(initialDelay, multiplier, maxDelay, true);
    }
}