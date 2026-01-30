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

package io.obsidian.json.error;

/**
 * Exception thrown when JSON parsing fails.
 *
 * <p>This exception indicates that the input is not valid JSON according
 * to the JSON specification. Common causes include:</p>
 * <ul>
 *   <li>Malformed JSON syntax</li>
 *   <li>Unexpected end of input</li>
 *   <li>Invalid escape sequences</li>
 *   <li>Incorrect use of commas or brackets</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class JsonParseException extends JsonException {

    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonParseException(String message, Throwable cause, JsonPath path) {
        super(message, cause, path);
    }
}
