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

package io.obsidian.promise.api;

/**
 * Represents the state of a Promise.
 *
 * <p>A Promise starts in PENDING state and transitions to exactly one final state:
 * <ul>
 *   <li>FULFILLED - completed successfully with a value</li>
 *   <li>REJECTED - completed with an error</li>
 *   <li>CANCELLED - was cancelled before completion</li>
 * </ul>
 *
 * <p>Once a Promise reaches a final state, it cannot change again.
 */
public enum PromiseState {

    /**
     * The Promise is still executing and has not completed.
     */
    PENDING,

    /**
     * The Promise completed successfully with a value.
     */
    FULFILLED,

    /**
     * The Promise completed with an error.
     */
    REJECTED,

    /**
     * The Promise was cancelled before completion.
     */
    CANCELLED;

    /**
     * Returns true if this is a final state (not PENDING).
     */
    public boolean isFinal() {
        return this != PENDING;
    }

    /**
     * Returns true if this represents a successful completion.
     */
    public boolean isSuccess() {
        return this == FULFILLED;
    }

    /**
     * Returns true if this represents a failure (rejected or cancelled).
     */
    public boolean isFailure() {
        return this == REJECTED || this == CANCELLED;
    }
}