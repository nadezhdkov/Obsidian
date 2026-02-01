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

package obsidian.json.error;

/**
 * Exception thrown when JSON validation fails.
 *
 * <p>This exception is thrown when JSON data doesn't meet validation
 * requirements, such as:</p>
 * <ul>
 *   <li>Required fields (@JsonRequired) are missing or null</li>
 *   <li>Custom validation rules fail</li>
 *   <li>Value constraints are violated</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class JsonValidationException extends JsonException {

    public JsonValidationException(String message) {
        super(message);
    }

    public JsonValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonValidationException(String message, Throwable cause, JsonPath path) {
        super(message, cause, path);
    }
}
