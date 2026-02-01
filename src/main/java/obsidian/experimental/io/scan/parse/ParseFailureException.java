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

/**
 * Exception thrown when raw input cannot be parsed into the expected type.
 *
 * <h2>Overview</h2>
 * {@code ParseFailureException} represents failures that occur during the parsing stage
 * of input processing.
 *
 * <p>
 * Parsing is responsible only for converting raw textual input into a typed value.
 * Validation rules are handled separately by {@link obsidian.experimental.io.scan.validate.Validator}.
 *
 * <h2>When this exception is used</h2>
 * A {@code ParseFailureException} should be thrown when:
 * <ul>
 *   <li>input does not conform to the expected format</li>
 *   <li>conversion to the target type fails</li>
 *   <li>input is syntactically invalid</li>
 * </ul>
 *
 * <p>
 * This exception is intentionally unchecked to allow parsers to be expressed
 * concisely using lambdas.
 *
 * <h2>Interaction with ScannerEngine</h2>
 * When thrown during interactive scanning:
 * <ul>
 *   <li>the input is considered invalid</li>
 *   <li>an "invalid entry" message is displayed</li>
 *   <li>the user is re-prompted</li>
 * </ul>
 *
 * @see Parser
 */
public class ParseFailureException extends RuntimeException {
    public ParseFailureException(String message) {
        super(message);
    }

    public ParseFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}