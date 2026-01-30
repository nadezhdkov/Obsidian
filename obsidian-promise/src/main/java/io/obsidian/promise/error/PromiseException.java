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

package io.obsidian.promise.error;

/**
 * Represents an exception that occurs when handling promises or asynchronous operations.
 * This is a runtime exception intended to encapsulate errors specific to promise execution.
 * <p>
 * This class provides constructors for creating an exception with no arguments, a detailed message,
 * a cause, or both a detailed message and a cause.
 */
public class PromiseException extends RuntimeException {

    public PromiseException() {
        super();
    }

    public PromiseException(String message) {
        super(message);
    }

    public PromiseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromiseException(Throwable cause) {
        super(cause);
    }
}