package io.obsidian.promise.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents an asynchronous computation that will eventually produce a value of type T,
 * fail with an error, or be cancelled.
 *
 * <p>Promise is immutable and thread-safe. Each transformation creates a new Promise instance.
 *
 * @param <T> the type of the computed value
 */
public interface Promise<T> {

    // ==================== Transformations ====================

    /**
     * Transforms the success value using the given function.
     *
     * @param mapper the transformation function
     * @param <U> the type of the transformed value
     * @return a new Promise with the transformed value
     */
    <U> Promise<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Chains another asynchronous operation that depends on this Promise's result.
     *
     * @param mapper function that returns a new Promise
     * @param <U> the type of the new Promise's value
     * @return a new Promise representing the chained operation
     */
    <U> Promise<U> flatMap(Function<? super T, ? extends Promise<U>> mapper);

    /**
     * Alias for flatMap, providing a more familiar API for some users.
     */
    default <U> Promise<U> then(Function<? super T, ? extends Promise<U>> mapper) {
        return flatMap(mapper);
    }

    /**
     * Transforms the success value, returning a new Promise with the same type.
     * Useful for side effects that return a new Promise.
     */
    default Promise<T> thenDo(Function<? super T, ? extends Promise<T>> action) {
        return flatMap(action);
    }

    /**
     * Executes a side effect when the Promise succeeds, without changing the value.
     *
     * @param consumer the side effect to execute
     * @return a new Promise with the same value
     */
    Promise<T> tap(Consumer<? super T> consumer);

    /**
     * Filters the result based on a predicate.
     * If the predicate returns false, the Promise is rejected.
     *
     * @param predicate the condition to test
     * @return a new Promise that may be rejected if predicate fails
     */
    Promise<T> filter(Predicate<? super T> predicate);

    /**
     * Filters with a custom error message.
     */
    Promise<T> filter(Predicate<? super T> predicate, Supplier<Throwable> errorSupplier);

    // ==================== Error Handling ====================

    /**
     * Handles errors by transforming them into successful values.
     *
     * @param recoveryFunction function to transform error into value
     * @return a new Promise that recovers from errors
     */
    Promise<T> recover(Function<Throwable, ? extends T> recoveryFunction);

    /**
     * Handles errors by executing alternative asynchronous operations.
     *
     * @param recoveryFunction function that returns a fallback Promise
     * @return a new Promise representing recovery
     */
    Promise<T> recoverWith(Function<Throwable, ? extends Promise<T>> recoveryFunction);

    /**
     * Catches and handles specific types of errors.
     *
     * @param errorType the type of error to catch
     * @param handler the error handler
     * @param <E> the error type
     * @return a new Promise that handles the specific error
     */
    <E extends Throwable> Promise<T> catchError(Class<E> errorType, Function<E, ? extends T> handler);

    /**
     * Catches all errors and provides a handler.
     */
    Promise<T> catchError(Function<Throwable, ? extends T> handler);

    /**
     * Transforms errors without recovering (error stays error, just mapped).
     *
     * @param errorMapper function to transform the error
     * @return a new Promise with transformed error
     */
    Promise<T> mapError(Function<Throwable, Throwable> errorMapper);

    /**
     * Executes an action regardless of success or failure.
     * Similar to try-finally block.
     *
     * @param action the action to execute
     * @return a new Promise that executes the action
     */
    Promise<T> finallyDo(Runnable action);

    // ==================== Timing & Control ====================

    /**
     * Applies a timeout to this Promise.
     * If the Promise doesn't complete within the duration, it will be rejected
     * with a TimeoutException.
     *
     * @param duration the timeout duration
     * @return a new Promise with timeout applied
     */
    Promise<T> timeout(Duration duration);

    /**
     * Delays the completion of this Promise by the specified duration.
     *
     * @param duration the delay duration
     * @return a new Promise that completes after the delay
     */
    Promise<T> delay(Duration duration);

    /**
     * Retries this Promise according to the given policy.
     *
     * @param policy the retry policy
     * @return a new Promise with retry behavior
     */
    Promise<T> retry(RetryPolicy policy);

    /**
     * Retries a fixed number of times with no delay.
     */
    default Promise<T> retry(int maxAttempts) {
        return retry(RetryPolicy.configure().maxAttempts(maxAttempts).build());
    }

    // ==================== State & Cancellation ====================

    /**
     * Returns the current state of this Promise.
     */
    PromiseState state();

    /**
     * Checks if the Promise is still pending.
     */
    default boolean isPending() {
        return state() == PromiseState.PENDING;
    }

    /**
     * Checks if the Promise has completed successfully.
     */
    default boolean isFulfilled() {
        return state() == PromiseState.FULFILLED;
    }

    /**
     * Checks if the Promise has failed.
     */
    default boolean isRejected() {
        return state() == PromiseState.REJECTED;
    }

    /**
     * Checks if the Promise was cancelled.
     */
    default boolean isCancelled() {
        return state() == PromiseState.CANCELLED;
    }

    /**
     * Attempts to cancel this Promise.
     * Returns true if cancellation was successful, false otherwise.
     */
    boolean cancel();

    /**
     * Cancels with a specific reason.
     */
    boolean cancel(String reason);

    // ==================== Callbacks ====================

    /**
     * Registers a callback for successful completion.
     *
     * @param onSuccess callback to execute on success
     * @return this Promise for chaining
     */
    Promise<T> onSuccess(Consumer<? super T> onSuccess);

    /**
     * Registers a callback for failure.
     *
     * @param onError callback to execute on error
     * @return this Promise for chaining
     */
    Promise<T> onError(Consumer<Throwable> onError);

    /**
     * Registers a callback for cancellation.
     *
     * @param onCancelled callback to execute on cancellation
     * @return this Promise for chaining
     */
    Promise<T> onCancelled(Runnable onCancelled);

    /**
     * Registers a callback that executes on any completion (success, error, or cancellation).
     *
     * @param onComplete callback to execute
     * @return this Promise for chaining
     */
    Promise<T> onComplete(Runnable onComplete);

    // ==================== Blocking Operations ====================

    /**
     * Blocks until the Promise completes and returns the result.
     * Throws the underlying exception if the Promise failed.
     *
     * @return the computed value
     * @throws Throwable if the Promise failed
     */
    T get() throws Throwable;

    /**
     * Blocks with a timeout.
     *
     * @param timeout the maximum time to wait
     * @return the computed value
     * @throws Throwable if the Promise failed or timed out
     */
    T get(Duration timeout) throws Throwable;

    /**
     * Blocks until completion and returns the value or a default on error.
     *
     * @param defaultValue the default value to return on error
     * @return the computed value or default
     */
    T getOrDefault(T defaultValue);

    /**
     * Blocks until completion and returns the value or computes a default.
     *
     * @param defaultSupplier supplier for default value
     * @return the computed value or default
     */
    T getOrElse(Supplier<? extends T> defaultSupplier);

    // ==================== Interop ====================

    /**
     * Converts this Promise to a CompletableFuture.
     *
     * @return a CompletableFuture representing this Promise
     */
    CompletableFuture<T> toCompletableFuture();

    /**
     * Converts this Promise to a standard Java Future.
     */
    default Future<T> toFuture() {
        return toCompletableFuture();
    }
}