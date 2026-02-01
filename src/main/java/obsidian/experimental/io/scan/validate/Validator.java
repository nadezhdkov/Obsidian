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

import obsidian.experimental.io.scan.InputHandler;

/**
 * Functional interface responsible for validating parsed values.
 *
 * <h2>Overview</h2>
 * {@code Validator} defines the validation phase of the input pipeline.
 *
 * <p>
 * A validator receives a fully parsed value and verifies whether it satisfies
 * domain-specific constraints.
 *
 * <h2>Validation vs parsing</h2>
 * <ul>
 *   <li><b>Parsing</b> — converts text into a value (syntax)</li>
 *   <li><b>Validation</b> — checks whether the value is acceptable (semantics)</li>
 * </ul>
 *
 * <p>
 * Validators should not perform parsing or input conversion.
 *
 * <h2>Failure handling</h2>
 * If validation fails, implementations must throw {@link ValidationException}.
 *
 * <p>
 * This allows {@link InputHandler} implementations
 * to handle invalid values uniformly.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Validator<Integer> positive = v -> {
 *     if (v <= 0) throw new ValidationException("Must be positive");
 * };
 * }</pre>
 *
 * @param <T> the type of value being validated
 *
 * @see ValidationException
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates the given value.
     *
     * @param value the parsed value to validate
     * @throws ValidationException if the value does not meet validation criteria
     */
    void validate(T value) throws ValidationException;
}
