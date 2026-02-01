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

package obsidian.experimental.io.scan.validate;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Factory utilities for common {@link Validator} implementations.
 *
 * <h2>Overview</h2>
 * {@code Validators} provides reusable validation rules that can be combined
 * with parsers during interactive input scanning.
 *
 * <p>
 * Validators focus exclusively on semantic correctness and never perform parsing.
 *
 * <h2>Usage examples</h2>
 *
 * <pre>{@code
 * int level = scan.until(
 *     "Level: ",
 *     Parsers.i32(),
 *     Validators.range(1, 100)
 * );
 * }</pre>
 *
 * <h2>Provided validators</h2>
 * <ul>
 *   <li>{@link #alwaysOk()}</li>
 *   <li>{@link #notBlank()}</li>
 *   <li>{@link #range(int, int)}</li>
 * </ul>
 *
 * @see Validator
 * @see ValidationException
 */
public final class Validators {

    private Validators() {}

    /**
     * Returns a validator that always succeeds.
     *
     * <p>
     * Useful as a default or placeholder when no validation is required.
     */
    @Contract(pure = true)
    public static <T> @NotNull Validator<T> alwaysOk() {
        return v -> {};
    }

    /**
     * Validates that a string is not {@code null} and not blank.
     *
     * @throws ValidationException if the string is null or blank.
     */
    @Contract(pure = true)
    public static @NotNull Validator<String> notBlank() {
        return s -> {
            if (s == null || s.isBlank()) throw new ValidationException("Value cannot be blank");
        };
    }

    /**
     * Validates that an integer value lies within the given inclusive range.
     *
     * @param min minimum allowed value
     * @param max maximum allowed value
     *
     * @throws ValidationException if the value is outside the allowed range.
     */
    @Contract(pure = true)
    public static @NotNull Validator<Integer> range(int min, int max) {
        return v -> {
            if (v < min || v > max) throw new ValidationException("Expected range " + min + ".." + max);
        };
    }
}