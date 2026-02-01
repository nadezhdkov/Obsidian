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

package obsidian.json.annotations;

import obsidian.json.error.JsonValidationException;
import java.lang.annotation.*;

/**
 * Marks a field as required during JSON deserialization.
 *
 * <p>When a field is marked as required, deserialization will fail with
 * a {@link JsonValidationException} if:</p>
 * <ul>
 *   <li>The field is missing from the JSON input</li>
 *   <li>The field value is null in the JSON input</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class User {
 *     @JsonRequired
 *     private String id;  // Must be present in JSON
 *
 *     @JsonRequired
 *     private String username;  // Must be present in JSON
 *
 *     private String nickname;  // Optional field
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonRequired {
}