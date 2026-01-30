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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an exception that aggregates multiple {@link Throwable} instances.
 * <p>
 * This exception is designed specifically to encapsulate and report multiple errors
 * that occur during the execution of asynchronous operations, such as promises.
 * Instead of throwing multiple exceptions individually, an {@code AggregateException}
 * wraps these errors into a single exception to simplify error handling.
 * <p>
 * Each instance of this exception provides access to the list of aggregated errors,
 * the number of errors, and the first encountered error for convenience.
 * <p>
 * The exception message is dynamically generated based on the aggregated errors
 * to provide a concise summary, including details for up to three errors and
 * an indication of additional errors if present.
 */
public class AggregateException extends PromiseException {

    private final List<Throwable> errors;

    public AggregateException(List<Throwable> errors) {
        super(buildMessage(errors));
        this.errors = new ArrayList<>(errors);
    }

    public AggregateException(String message, List<Throwable> errors) {
        super(message);
        this.errors = new ArrayList<>(errors);
    }

    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public int getErrorCount() {
        return errors.size();
    }

    public Throwable getFirstError() {
        return errors.isEmpty() ? null : errors.getFirst();
    }

    private static @NotNull String buildMessage(List<Throwable> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Multiple errors occurred";
        }

        if (errors.size() == 1) {
            return "1 error occurred: " + errors.get(0).getMessage();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(errors.size()).append(" errors occurred:");

        for (int i = 0; i < Math.min(errors.size(), 3); i++) {
            sb.append("\n  ").append(i + 1).append(". ");
            sb.append(errors.get(i).getClass().getSimpleName());
            String msg = errors.get(i).getMessage();
            if (msg != null && !msg.isEmpty()) {
                sb.append(": ").append(msg);
            }
        }

        if (errors.size() > 3) {
            sb.append("\n  ... and ").append(errors.size() - 3).append(" more");
        }

        return sb.toString();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        System.err.println("Aggregated errors:");
        for (int i = 0; i < errors.size(); i++) {
            System.err.println("  [" + (i + 1) + "]");
            errors.get(i).printStackTrace();
        }
    }
}