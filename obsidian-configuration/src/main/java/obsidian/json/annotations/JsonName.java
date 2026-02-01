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

import java.lang.annotation.*;

/**
 * Specifies the name of a field as it appears in JSON.
 *
 * <p>Use this annotation when the JSON field name differs from the Java
 * field name. This is common when dealing with APIs that use different
 * naming conventions (e.g., snake_case vs camelCase).</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class User {
 *     @JsonName("user_name")
 *     private String username;
 *
 *     @JsonName("first_name")
 *     private String firstName;
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonName {

    /**
     * The name of the field in JSON.
     *
     * @return the JSON field name
     */
    String value();
}