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

package io.obsidian.json.annotations;

import java.lang.annotation.*;

/**
 * Marks a field to be ignored during JSON serialization and deserialization.
 *
 * <p>Use this annotation when you have fields that should not be included
 * in JSON output or should not be populated from JSON input.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class User {
 *     private String username;
 *
 *     @JsonIgnore
 *     private String password;  // Never serialized to JSON
 *
 *     @JsonIgnore
 *     private transient String cachedData;
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonIgnore {
}