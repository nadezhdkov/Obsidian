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

package io.obsidian.json.api;

import io.obsidian.json.internal.gson.GsonMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Main entry point for the Obsidian JSON API.
 *
 * <p>This facade provides access to JSON mapping functionality while hiding
 * the underlying implementation details. The system uses Google Gson as the
 * internal engine but does not expose it in the public API.</p>
 *
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Using default mapper
 * JsonMapper mapper = Json.defaultMapper();
 *
 * // Creating custom mapper
 * JsonMapper custom = Json.configure()
 *     .prettyPrint(true)
 *     .failOnUnknownFields(false)
 *     .enableAnnotations(true)
 *     .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class Json {

    private static final JsonMapper DEFAULT_MAPPER = new GsonMapper(JsonConfig.defaultConfig());

    private Json() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the default JSON mapper with standard configuration.
     *
     * <p>The default mapper uses these settings:</p>
     * <ul>
     *   <li>Pretty print: disabled</li>
     *   <li>Serialize nulls: disabled</li>
     *   <li>Lenient parsing: disabled</li>
     *   <li>Annotations: enabled (Obsidian only)</li>
     * </ul>
     *
     * @return the default JSON mapper instance
     */
    public static JsonMapper defaultMapper() {
        return DEFAULT_MAPPER;
    }

    /**
     * Creates a new builder for customizing JSON mapper configuration.
     *
     * @return a new configuration builder
     */
    @Contract(value = " -> new", pure = true)
    public static JsonConfig.@NotNull Builder configure() {
        return JsonConfig.builder();
    }

    /**
     * Creates a JSON mapper with the specified configuration.
     *
     * @param config the configuration to use
     * @return a new JSON mapper instance
     * @throws IllegalArgumentException if config is null
     */
    @Contract("null -> fail; !null -> new")
    public static @NotNull JsonMapper mapper(JsonConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        return new GsonMapper(config);
    }
}