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

package obsidian.util.problem;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Map;

/**
 * Structured error metadata abstraction.
 *
 * <h2>Overview</h2>
 * {@code ErrorMetadata} represents a structured, serializable-friendly description
 * of an error, suitable for logging, diagnostics, APIs and problem-reporting systems.
 *
 * <p>
 * Unlike raw {@link Throwable}, this interface focuses on:
 * <ul>
 *   <li>Stable error identifiers ({@link #code()})</li>
 *   <li>Human-readable messages ({@link #message()})</li>
 *   <li>Optional root cause ({@link #cause()})</li>
 *   <li>Additional contextual data ({@link #meta()})</li>
 * </ul>
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li><b>Explicit</b>: errors carry structured, typed information</li>
 *   <li><b>Serializable-friendly</b>: metadata should be simple and small</li>
 *   <li><b>Log-oriented</b>: optimized for readable output via {@link #pretty()}</li>
 *   <li><b>Throwable-agnostic</b>: usable even when no exception is involved</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * ErrorMetadata err = new SimpleError(
 *     "USER_NOT_FOUND",
 *     "User with id 42 does not exist",
 *     Map.of("userId", 42),
 *     null
 * );
 *
 * log.error(err.pretty());
 * }</pre>
 *
 * <h2>Relation to exceptions</h2>
 * <p>
 * {@link #cause()} is optional. This allows {@code ErrorMetadata} to represent
 * both exception-based and domain-level errors.
 *
 * @see Throwable
 */
public interface ErrorMetadata {

    /**
     * Stable machine-readable error code.
     *
     * <p>
     * This value should be suitable for programmatic handling,
     * API responses and localization keys.
     *
     * @return non-null error code
     */
    @NotNull String code();

    /**
     * Human-readable error message.
     *
     * <p>
     * This message is intended for logs or user-facing output,
     * depending on context.
     *
     * @return non-null error message
     */
    @NotNull String message();

    /**
     * Optional underlying cause of the error.
     *
     * <p>
     * May be {@code null} for domain-level or validation errors.
     *
     * @return the underlying throwable, or {@code null} if none
     */
    @Nullable Throwable cause();

    /**
     * Optional structured metadata providing additional context.
     *
     * <p>
     * Typical entries include identifiers, parameters, or small diagnostic values.
     * Implementations should keep this map:
     * <ul>
     *   <li>Small</li>
     *   <li>Serializable-friendly</li>
     *   <li>Free of heavy objects</li>
     * </ul>
     *
     * @return non-null metadata map (may be empty)
     */
    @NotNull Map<String, Object> meta();

    /**
     * Returns a human-readable string representation suitable for logs or console output.
     *
     * <p>
     * The default format includes:
     * <ul>
     *   <li>Error code</li>
     *   <li>Message</li>
     *   <li>Metadata (if present)</li>
     *   <li>Cause class and message (if present)</li>
     * </ul>
     *
     * <p>Example output:</p>
     * <pre>{@code
     * [USER_NOT_FOUND] User with id 42 does not exist meta={userId=42}
     * }</pre>
     *
     * @return formatted error string
     */
    default @NotNull String pretty() {
        String metaStr  = meta().isEmpty() ? "" : " meta=" + meta();
        String causeStr = (cause() == null)
                ? ""
                : " cause=" + Objects.requireNonNull(cause()).getClass().getSimpleName()
                + ": " + Objects.requireNonNull(cause()).getMessage();

        return "[" + code() + "] " + message() + metaStr + causeStr;
    }
}
