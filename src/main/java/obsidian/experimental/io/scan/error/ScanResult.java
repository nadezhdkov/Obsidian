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

package obsidian.experimental.io.scan.error;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Optional;
import java.util.Objects;

/**
 * Represents the result of an operation that may either succeed with a value
 * or fail with a structured {@link Error}.
 *
 * <h2>Concept</h2>
 * {@code ResultWrapper} is a functional-style container inspired by constructs such as:
 * <ul>
 *   <li>{@code Result} (Rust)</li>
 *   <li>{@code Either}</li>
 *   <li>{@code Try}</li>
 * </ul>
 *
 * Instead of throwing exceptions immediately, operations may return a
 * {@code ResultWrapper} describing either:
 * <ul>
 *   <li>a successful value ({@link #ok(Object)})</li>
 *   <li>or a failure ({@link #fail(Error)})</li>
 * </ul>
 *
 * This allows error handling to be explicit, composable, and deferred.
 *
 * <h2>Success vs Error</h2>
 * At any time, a {@code ResultWrapper} is in exactly one state:
 * <ul>
 *   <li><b>OK</b>: contains a non-null value and no error</li>
 *   <li><b>Error</b>: contains an {@link Error} and no value</li>
 * </ul>
 *
 * The state can be checked using {@link #isOk()} and {@link #isError()}.
 *
 * <h2>Examples</h2>
 *
 * <h3>Basic usage</h3>
 * <pre>{@code
 * ResultWrapper<Integer> result = parseNumber(input);
 *
 * if (result.isOk()) {
 *     int value = result.orElseThrow();
 * } else {
 *     System.err.println(result.error().pretty());
 * }
 * }</pre>
 *
 * <h3>Fallback value</h3>
 * <pre>{@code
 * int value = result.orElse(0);
 * }</pre>
 *
 * <h3>Functional mapping</h3>
 * <pre>{@code
 * ResultWrapper<String> text =
 *     result.map(n -> "Value = " + n);
 * }</pre>
 *
 * <h3>Side-effect on error</h3>
 * <pre>{@code
 * result.onError(err -> logger.error(err.pretty()));
 * }</pre>
 *
 * <h3>Exception bridging</h3>
 * Convert a structured error into an exception when required:
 *
 * <pre>{@code
 * int value = result.orElseThrow(
 *     err -> new IllegalStateException(err.pretty(), err.cause)
 * );
 * }</pre>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>No exceptions are thrown unless explicitly requested.</li>
 *   <li>The API encourages explicit error handling.</li>
 *   <li>{@link Error} retains structured information and root cause.</li>
 *   <li>{@link #map(Function)} preserves error state automatically.</li>
 * </ul>
 *
 * @param <T> the type of the success value
 */
public final class ScanResult<T> {

    private final T         value;
    private final Error     error;

    private ScanResult(T value, Error error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a successful result containing the given value.
     *
     * @param value the success value (may be null).
     * @param <T> the value type.
     * @return a successful {@code ResultWrapper}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull ScanResult<T> ok(T value) {
        return new ScanResult<>(value, null);
    }

    /**
     * Creates a failed result containing the given {@link Error}.
     *
     * @param error the error describing the failure.
     * @param <T> the expected success type.
     * @return a failed {@code ResultWrapper}.
     */
    @Contract("_ -> new")
    public static <T> @NotNull ScanResult<T> fail(Error error) {
        return new ScanResult<>(null, Objects.requireNonNull(error, "error"));
    }

    /**
     * @return {@code true} if this result represents a successful value.
     */
    public boolean isOk() {
        return error == null;
    }

    /**
     * @return {@code true} if this result represents an error.
     */
    public boolean isError() {
        return error != null;
    }

    /**
     * Returns the wrapped value if successful, otherwise returns the given fallback.
     *
     * @param fallback value to return if this result is an error.
     * @return the success value or fallback.
     */
    public T orElse(T fallback) {
        return isOk() ? value : fallback;
    }

    /**
     * Returns the wrapped value if successful, otherwise {@code null}.
     *
     * @return the value or null if an error occurred.
     */
    @Contract(pure = true)
    public @Nullable T orNull() {
        return isOk() ? value : null;
    }

    /**
     * Converts this result into an {@link Optional}.
     *
     * @return an optional containing the value if successful, otherwise empty.
     */
    public Optional<T> toOptional() {
        return isOk() ? Optional.ofNullable(value) : Optional.empty();
    }

    /**
     * Retrieves the error associated with this result, if it represents a failure.
     * If this result is successful, this method will return null or an undefined state
     * depending on the implementation.
     *
     * @return the {@link java.lang.Error} instance describing the failure, or null if this result is successful.
     */
    public Error error() {
        return error;
    }

    /**
     * Returns the value if successful or throws a {@link RuntimeException}
     * containing the error description.
     *
     * @throws RuntimeException if this result represents an error.
     */
    public T orElseThrow() {
        if (isOk()) return value;
        throw new RuntimeException(error.pretty(), error.cause);
    }

    /**
     * Returns the value if successful or throws a custom exception created
     * from the contained {@link Error}.
     *
     * @param exFn function mapping an {@link Error} into a {@link RuntimeException}.
     * @throws RuntimeException if this result represents an error.
     */
    public T orElseThrow(Function<Error, ? extends RuntimeException> exFn) {
        if (isOk()) return value;
        throw exFn.apply(error);
    }

    /**
     * Executes the given consumer if this result represents an error.
     *
     * @param c consumer to execute on error.
     * @return this same result instance.
     */
    public ScanResult<T> onError(Consumer<Error> c) {
        if (isError()) c.accept(error);
        return this;
    }

    /**
     * Maps the success value to another type.
     * <p>
     * If this result represents an error, the same error is propagated.
     *
     * @param fn mapping function applied to the success value.
     * @param <U> the new success type.
     * @return a new mapped result or the same error result.
     */
    public <U> ScanResult<U> map(Function<? super T, ? extends U> fn) {
        if (isError()) return fail(error);
        return ok(fn.apply(value));
    }
}