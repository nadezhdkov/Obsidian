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
 * Exception thrown when I/O operations fail.
 *
 * <p>This exception wraps I/O errors that occur when reading from or
 * writing to external sources such as files or streams. Common causes:</p>
 * <ul>
 *   <li>File not found</li>
 *   <li>Permission denied</li>
 *   <li>Disk full</li>
 *   <li>Network errors</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class JsonIoException extends JsonException {

    public JsonIoException(String message) {
        super(message);
    }

    public JsonIoException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonIoException(String message, Throwable cause, JsonPath path) {
        super(message, cause, path);
    }
}
