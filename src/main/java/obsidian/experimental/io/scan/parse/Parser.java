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

package obsidian.experimental.io.scan.parse;

import obsidian.experimental.io.scan.InputHandler;

/**
 * Functional interface responsible for converting raw input into typed values.
 *
 * <h2>Overview</h2>
 * {@code Parser} defines the parsing phase of the input pipeline.
 *
 * <p>
 * It receives raw user input as a {@link String} and attempts to convert it
 * into a value of type {@code T}.
 *
 * <h2>Parsing vs validation</h2>
 * Parsing and validation are intentionally separated:
 *
 * <ul>
 *   <li><b>Parsing</b> — converts text into a value (syntax)</li>
 *   <li><b>Validation</b> — checks whether the value is acceptable (semantics)</li>
 * </ul>
 *
 * <p>
 * Parsers should focus only on conversion logic and should not enforce
 * domain-specific constraints.
 *
 * <h2>Failure handling</h2>
 * If parsing fails, implementations should throw {@link ParseFailureException}.
 *
 * <p>
 * This allows {@link InputHandler} implementations
 * to uniformly handle invalid input and re-prompt the user.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Parser<Integer> p = raw -> Integer.parseInt(raw.trim());
 * }</pre>
 *
 * @param <T> the target type produced by this parser
 *
 * @see ParseFailureException
 * @see obsidian.experimental.io.scan.validate.Validator
 */
@FunctionalInterface
public interface Parser<T> {

    /**
     * Parses the given raw input string into a value of type {@code T}.
     *
     * @param raw the raw input string
     * @return the parsed value
     * @throws ParseFailureException if parsing fails
     */
    T parse(String raw) throws ParseFailureException;
}
