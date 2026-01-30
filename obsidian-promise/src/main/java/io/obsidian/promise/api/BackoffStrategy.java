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

import io.obsidian.promise.api.internal.backoff.ExponentialBackoff;
import io.obsidian.promise.api.internal.backoff.FixedBackoff;
import io.obsidian.promise.api.internal.backoff.NoBackoff;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface BackoffStrategy {
    Duration calculateDelay(int attempt);

    BackoffStrategy withMaxDelay(Duration maxDelay);

    BackoffStrategy withJitter();

    @Contract(value = " -> new", pure = true)
    static @NotNull BackoffStrategy none() {
        return new NoBackoff();
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull BackoffStrategy fixed(Duration delay) {
        return new FixedBackoff(delay);
    }

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull BackoffStrategy exponential(Duration initialDelay, double multiplier) {
        return new ExponentialBackoff(initialDelay, multiplier, null, false);
    }
}