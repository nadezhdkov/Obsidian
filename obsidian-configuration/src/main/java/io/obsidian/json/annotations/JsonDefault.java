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
 * Specifies a default value for a field when it's absent or null in JSON.
 *
 * <p>This annotation provides a fallback value during deserialization.
 * The default value is specified as a string and will be converted to
 * the appropriate type.</p>
 *
 * <p>Supported types:</p>
 * <ul>
 *   <li>Primitives: int, long, double, float, boolean, etc.</li>
 *   <li>Strings</li>
 *   <li>Enums (by name)</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class ServerConfig {
 *     @JsonDefault("localhost")
 *     private String host;
 *
 *     @JsonDefault("8080")
 *     private int port;
 *
 *     @JsonDefault("true")
 *     private boolean enableSsl;
 *
 *     @JsonDefault("INFO")
 *     private LogLevel logLevel;
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonDefault {

    /**
     * The default value as a string.
     *
     * @return the default value
     */
    String value();
}