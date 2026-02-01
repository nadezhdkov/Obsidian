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

/**
 * Exception thrown when a parsed value fails validation.
 *
 * <h2>Overview</h2>
 * {@code ValidationException} represents semantic validation failures.
 *
 * <p>
 * While parsing ({@link obsidian.experimental.io.scan.parse.Parser}) is responsible
 * for converting raw textual input into typed values, validation is responsible
 * for ensuring that the parsed value satisfies domain rules.
 *
 * <h2>Typical use cases</h2>
 * A {@code ValidationException} should be thrown when:
 * <ul>
 *   <li>a value is outside an allowed range</li>
 *   <li>a string is blank or invalid</li>
 *   <li>a business rule is violated</li>
 * </ul>
 *
 * <h2>Interaction with ScannerEngine</h2>
 * When thrown during interactive scanning:
 * <ul>
 *   <li>the value is rejected</li>
 *   <li>an "invalid value" message is displayed</li>
 *   <li>the user is re-prompted</li>
 * </ul>
 *
 * <p>
 * This exception is intentionally unchecked to keep validator implementations concise.
 *
 * @see Validator
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}