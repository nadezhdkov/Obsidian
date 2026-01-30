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

import java.time.Duration;

/**
 * Signals that an operation has exceeded the allotted time limit.
 * <p>
 * This exception is specifically used in the context of asynchronous
 * operations or promises to indicate timeout scenarios.
 * <p>
 * Instances of this exception include the duration of the timeout
 * that caused the operation to fail, allowing detailed insight into
 * the exceeded threshold.
 */
public class TimeoutException extends PromiseException {

    private final Duration timeout;

    public TimeoutException(Duration timeout) {
        super("Operation timed out after " + timeout);
        this.timeout = timeout;
    }

    public TimeoutException(String message, Duration timeout) {
        super(message);
        this.timeout = timeout;
    }

    public Duration getTimeout() {
        return timeout;
    }
}