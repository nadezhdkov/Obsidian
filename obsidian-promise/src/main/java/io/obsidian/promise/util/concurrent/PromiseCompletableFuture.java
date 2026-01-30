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

package io.obsidian.promise.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * Engine that provides scheduling and execution services for promises.
 * Based on CompletableFuture and ScheduledExecutorService.
 */
public class PromiseCompletableFuture {

    private static final PromiseCompletableFuture INSTANCE = new PromiseCompletableFuture();

    private final ScheduledExecutorService scheduler;
    private final Executor                 defaultExecutor;

    private PromiseCompletableFuture() {
        this.scheduler       = createScheduler();
        this.defaultExecutor = ForkJoinPool.commonPool();
    }

    public static PromiseCompletableFuture getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the default executor for async operations.
     */
    public Executor defaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Returns the scheduler for delayed operations.
     */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /**
     * Schedules a task to run after a delay.
     *
     * @param task the task to run
     * @param delay the delay
     * @return a ScheduledFuture representing the scheduled task
     */
    public ScheduledFuture<?> schedule(Runnable task, @NotNull Duration delay) {
        return scheduler.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a callable to run after a delay.
     */
    public <T> ScheduledFuture<T> schedule(Callable<T> callable, @NotNull Duration delay) {
        return scheduler.schedule(callable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a task to run repeatedly with a fixed delay between executions.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, @NotNull Duration initialDelay, @NotNull Duration delay) {
        return scheduler.scheduleWithFixedDelay(
                task,
                initialDelay.toMillis(),
                delay.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, @NotNull Duration initialDelay, @NotNull Duration period) {
        return scheduler.scheduleAtFixedRate(
                task,
                initialDelay.toMillis(),
                period.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    private static @NotNull ScheduledExecutorService createScheduler() {
        int processors = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.max(2, processors / 2);

        return new ScheduledThreadPoolExecutor(
                corePoolSize,
                new ThreadFactory() {
                    private int counter = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "promise-scheduler-" + counter++);
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );
    }

    /**
     * Shuts down the engine (mainly for testing).
     * Not recommended for production use as it's a singleton.
     */
    public void shutdown() {
        scheduler.shutdown();
    }

    /**
     * Attempts to shut down immediately.
     */
    public void shutdownNow() {
        scheduler.shutdownNow();
    }
}