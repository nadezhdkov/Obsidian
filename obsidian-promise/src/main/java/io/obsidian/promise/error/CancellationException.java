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
 * Indicates that an operation has been cancelled.
 *
 * <p>This exception is specifically used in the context of asynchronous
 * operations or promises to signal that a process was intentionally aborted
 * before completion.
 *
 * <p>Typically, this exception is thrown when a cancellation token or similar mechanism
 * identifies that a cancellation request has been made and the operation should terminate.
 *
 * <p>CancellationException extends {@link PromiseException}, providing additional granularity
 * in differentiating between cancellation scenarios and other promise-related exceptions.
 *
 * <p>Constructors allow specifying a custom cancellation reason or cause, in addition
 * to the default cancellation message.
 */
public class CancellationException extends PromiseException {

    public CancellationException() {
        super("Operation was cancelled");
    }

    public CancellationException(String reason) {
        super(reason != null ? reason : "Operation was cancelled");
    }

    public CancellationException(String message, Throwable cause) {
        super(message, cause);
    }

}