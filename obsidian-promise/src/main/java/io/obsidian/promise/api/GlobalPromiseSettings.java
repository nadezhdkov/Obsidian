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

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Global configuration for Promise behavior.
 *
 * <p>This class allows customizing default settings like executors,
 * timeouts, and retry policies.
 */
public class GlobalPromiseSettings {

    private static final GlobalPromiseSettings INSTANCE = new GlobalPromiseSettings();

    private Executor    defaultExecutor;
    private Duration    defaultTimeout;
    private RetryPolicy defaultRetryPolicy;
    private boolean     enableMetrics;
    private boolean     enableTracing;

    private GlobalPromiseSettings() {
        this.defaultExecutor    = null;
        this.defaultTimeout     = null;
        this.defaultRetryPolicy = RetryPolicy.simple(3);
        this.enableMetrics      = false;
        this.enableTracing      = false;
    }

    /**
     * Returns the global configuration instance.
     */
    public static GlobalPromiseSettings global() {
        return INSTANCE;
    }

    /**
     * Sets the default executor for async operations.
     */
    public GlobalPromiseSettings setDefaultExecutor(Executor executor) {
        this.defaultExecutor = executor;
        return this;
    }

    /**
     * Gets the default executor.
     */
    public Executor getDefaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Sets the default timeout for all promises.
     */
    public GlobalPromiseSettings setDefaultTimeout(Duration timeout) {
        this.defaultTimeout = timeout;
        return this;
    }

    /**
     * Gets the default timeout.
     */
    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * Sets the default retry policy.
     */
    public GlobalPromiseSettings setDefaultRetryPolicy(RetryPolicy policy) {
        this.defaultRetryPolicy = policy;
        return this;
    }

    /**
     * Gets the default retry policy.
     */
    public RetryPolicy getDefaultRetryPolicy() {
        return defaultRetryPolicy;
    }

    /**
     * Enables or disables metrics collection.
     */
    public GlobalPromiseSettings setEnableMetrics(boolean enable) {
        this.enableMetrics = enable;
        return this;
    }

    /**
     * Returns true if metrics are enabled.
     */
    public boolean isMetricsEnabled() {
        return enableMetrics;
    }

    /**
     * Enables or disables distributed tracing.
     */
    public GlobalPromiseSettings setEnableTracing(boolean enable) {
        this.enableTracing = enable;
        return this;
    }

    /**
     * Returns true if tracing is enabled.
     */
    public boolean isTracingEnabled() {
        return enableTracing;
    }

    /**
     * Resets configuration to defaults.
     */
    public void reset() {
        this.defaultExecutor = null;
        this.defaultTimeout = null;
        this.defaultRetryPolicy = RetryPolicy.simple(3);
        this.enableMetrics = false;
        this.enableTracing = false;
    }
}