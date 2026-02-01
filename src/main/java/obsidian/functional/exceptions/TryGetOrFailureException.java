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

package obsidian.functional.exceptions;

/**
 * Unchecked exception thrown when attempting to {@code get()} the value of a failed {@link obsidian.functional.Try}.
 *
 * <h2>Overview</h2>
 * {@code TryGetOrFailureException} is used internally by {@link obsidian.functional.Try.Failure}
 * to provide fast failure propagation when user code calls {@code get()} on a failure.
 *
 * <p>
 * This is an intentional design choice: {@link obsidian.functional.Try#get()} is meant to be
 * a "success-only" accessor. If the computation failed, calling {@code get()} is considered
 * a misuse and results in this exception being thrown.
 *
 * <h2>Recommended usage</h2>
 * Instead of calling {@code get()} directly, prefer using:
 * <ul>
 *   <li>{@link obsidian.functional.Try#getOrElse(Object)} for fallback values</li>
 *   <li>{@link obsidian.functional.Try#recover(java.util.function.Function)} to recover from failures</li>
 *   <li>{@link obsidian.functional.Try#getOrThrow(java.util.function.Function)} to map failure into a custom exception</li>
 *   <li>{@link obsidian.functional.Try#checkedGet()} if you want to rethrow checked exceptions</li>
 * </ul>
 *
 * <h2>Error cause</h2>
 * The original failure cause is preserved as {@link #getCause()}.
 *
 * @see obsidian.functional.Try
 * @see obsidian.functional.Try.Failure
 */
public class TryGetOrFailureException extends RuntimeException {

    /**
     * Creates a new exception for the given failure cause.
     *
     * @param cause the underlying error that caused the {@link obsidian.functional.Try} to fail
     */
    public TryGetOrFailureException(Throwable cause) {
        super("Tried to get() from a Failure", cause);
    }
}

