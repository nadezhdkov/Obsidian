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

package io.obsidian.json.error;

/**
 * Base exception for all JSON-related errors.
 *
 * <p>This is the root of the JSON exception hierarchy. All exceptions
 * thrown by the Obsidian JSON library extend this class.</p>
 *
 * @since 1.0.0
 */
public class JsonException extends RuntimeException {

    private final JsonPath path;

    /**
     * Creates a new exception with a message.
     *
     * @param message the error message
     */
    public JsonException(String message) {
        this(message, null, null);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public JsonException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Creates a new exception with a message, cause, and path.
     *
     * @param message the error message
     * @param cause the underlying cause
     * @param path the JSON path where the error occurred
     */
    public JsonException(String message, Throwable cause, JsonPath path) {
        super(formatMessage(message, path), cause);
        this.path = path;
    }

    /**
     * Gets the JSON path where the error occurred, if available.
     *
     * @return the JSON path, or null if not available
     */
    public JsonPath getPath() {
        return path;
    }

    /**
     * Checks if this exception has path information.
     *
     * @return true if path is available
     */
    public boolean hasPath() {
        return path != null;
    }

    private static String formatMessage(String message, JsonPath path) {
        if (path != null && !path.isEmpty()) {
            return message + " at path: " + path;
        }
        return message;
    }
}