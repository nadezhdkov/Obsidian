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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable default implementation of {@link ErrorMetadata}.
 *
 * <h2>Overview</h2>
 * {@code ErrorInfo} is a concrete, immutable value object that represents
 * structured error information, including:
 * <ul>
 *   <li>A stable error code</li>
 *   <li>A human-readable message</li>
 *   <li>An optional underlying cause</li>
 *   <li>Optional structured metadata</li>
 * </ul>
 *
 * <p>
 * It is designed to be:
 * <ul>
 *   <li><b>Immutable</b>: all instances are thread-safe</li>
 *   <li><b>Log-friendly</b>: integrates with {@link ErrorMetadata#pretty()}</li>
 *   <li><b>Serializable-friendly</b>: metadata is copied and wrapped unmodifiable</li>
 * </ul>
 *
 * <h2>Creation</h2>
 * Instances are created using factory methods:
 *
 * <pre>{@code
 * ErrorInfo err = ErrorInfo.of(
 *     "INVALID_INPUT",
 *     "Input value is not valid"
 * );
 * }</pre>
 *
 * <h2>With cause</h2>
 * <pre>{@code
 * ErrorInfo err = ErrorInfo.of(
 *     "IO_ERROR",
 *     "Failed to read configuration",
 *     exception
 * );
 * }</pre>
 *
 * <h2>With metadata</h2>
 * <pre>{@code
 * ErrorInfo err = ErrorInfo.of(
 *     "USER_NOT_FOUND",
 *     "User does not exist",
 *     null,
 *     Map.of("userId", 42)
 * );
 * }</pre>
 *
 * <h2>Fluent metadata enrichment</h2>
 * Additional metadata can be added fluently using {@link #with(String, Object)}:
 *
 * <pre>{@code
 * err = err.with("attempt", 3)
 *          .with("source", "login-form");
 * }</pre>
 *
 * Each call returns a <b>new</b> instance.
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>Metadata order is preserved using {@link LinkedHashMap}</li>
 *   <li>Metadata map is defensively copied and immutable</li>
 *   <li>Null keys and values are rejected</li>
 * </ul>
 *
 * @see ErrorMetadata
 */
public final class ErrorInfo implements ErrorMetadata {

    private final String              code;
    private final String              message;
    private final Throwable           cause;
    private final Map<String, Object> meta;

    private ErrorInfo(String code, String message, Throwable cause, Map<String, Object> meta) {
        this.code    = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.cause   = cause;
        this.meta    = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(meta, "meta"))
        );
    }

    /**
     * Creates an {@code ErrorInfo} with the given code and message.
     *
     * @param code    stable error code
     * @param message human-readable message
     * @return new {@code ErrorInfo} instance
     */
    public static @NotNull ErrorInfo of(@NotNull String code, @NotNull String message) {
        return new ErrorInfo(code, message, null, Map.of());
    }

    /**
     * Creates an {@code ErrorInfo} with the given code, message and cause.
     *
     * @param code    stable error code
     * @param message human-readable message
     * @param cause   underlying cause (may be {@code null})
     * @return new {@code ErrorInfo} instance
     */
    public static @NotNull ErrorInfo of(
            @NotNull String     code,
            @NotNull String     message,
            @Nullable Throwable cause
    ) {
        return new ErrorInfo(code, message, cause, Map.of());
    }

    /**
     * Creates an {@code ErrorInfo} with full configuration.
     *
     * @param code    stable error code
     * @param message human-readable message
     * @param cause   underlying cause (may be {@code null})
     * @param meta    structured metadata (must not be {@code null})
     * @return new {@code ErrorInfo} instance
     */
    public static @NotNull ErrorInfo of(
            @NotNull  String              code,
            @NotNull  String              message,
            @Nullable Throwable           cause,
            @NotNull  Map<String, Object> meta
    ) {
        return new ErrorInfo(code, message, cause, meta);
    }

    /**
     * Returns a new {@code ErrorInfo} with an additional metadata entry.
     *
     * <p>
     * The original instance remains unchanged.
     *
     * @param key   metadata key
     * @param value metadata value
     * @return new {@code ErrorInfo} with the added metadata
     */
    @Contract("_, _ -> new")
    public @NotNull ErrorInfo with(@NotNull String key, @NotNull Object value) {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>(this.meta);
        m.put(
                Objects.requireNonNull(key,   "key"),
                Objects.requireNonNull(value, "value")
        );
        return new ErrorInfo(code, message, cause, m);
    }

    @Override
    public @NotNull String              code() {
        return code;
    }

    @Override
    public @NotNull String              message() {
        return message;
    }

    @Override
    public @Nullable Throwable          cause() {
        return cause;
    }

    @Override
    public @NotNull Map<String, Object> meta() {
        return meta;
    }
}
