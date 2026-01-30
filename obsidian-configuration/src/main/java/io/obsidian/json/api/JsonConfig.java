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

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Immutable configuration for JSON mapping behavior.
 *
 * <p>This class defines all configuration options that affect how JSON is
 * parsed, serialized, and mapped to Java objects. Once created, a configuration
 * cannot be modified.</p>
 *
 * <p>Use the builder to create custom configurations:</p>
 * <pre>{@code
 * JsonConfig config = JsonConfig.builder()
 *     .prettyPrint(true)
 *     .serializeNulls(false)
 *     .lenient(true)
 *     .dateFormat("yyyy-MM-dd")
 *     .enableAnnotations(true)
 *     .annotationsMode(AnnotationsMode.OBSIDIAN_ONLY)
 *     .build();
 * }</pre>
 *
 * @since 1.0.0
 */
@Getter
public final class JsonConfig {

    private static final JsonConfig DEFAULT = builder().build();

    private final boolean         prettyPrint;
    private final boolean         serializeNulls;
    private final boolean         lenient;
    private final boolean         failOnUnknownFields;
    private final boolean         annotationsEnabled;
    private final boolean         htmlEscaping;
    private final String          dateFormat;
    private final AnnotationsMode annotationsMode;

    @Contract(pure = true)
    private JsonConfig(@NotNull Builder builder) {
        this.prettyPrint         = builder.prettyPrint;
        this.serializeNulls      = builder.serializeNulls;
        this.lenient             = builder.lenient;
        this.failOnUnknownFields = builder.failOnUnknownFields;
        this.dateFormat          = builder.dateFormat;
        this.annotationsEnabled  = builder.annotationsEnabled;
        this.annotationsMode     = builder.annotationsMode;
        this.htmlEscaping        = builder.htmlEscaping;
    }

    /**
     * Returns the default configuration instance.
     *
     * @return default configuration
     */
    public static JsonConfig defaultConfig() {
        return DEFAULT;
    }

    /**
     * Creates a new configuration builder.
     *
     * @return a new builder instance
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonConfig that = (JsonConfig) o;
        return prettyPrint         == that.prettyPrint &&
                serializeNulls      == that.serializeNulls &&
                lenient             == that.lenient &&
                failOnUnknownFields == that.failOnUnknownFields &&
                annotationsEnabled  == that.annotationsEnabled &&
                htmlEscaping        == that.htmlEscaping &&
                annotationsMode     == that.annotationsMode &&
                Objects.equals(dateFormat, that.dateFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                prettyPrint, serializeNulls, lenient,
                failOnUnknownFields, dateFormat,
                annotationsEnabled, annotationsMode, htmlEscaping
        );
    }

    @Override
    public String toString() {
        return "JsonConfig{" +
                "prettyPrint="          + prettyPrint +
                ", serializeNulls="     + serializeNulls +
                ", lenient="            + lenient +
                ", failOnUnknownFields="+ failOnUnknownFields +
                ", dateFormat='"        + dateFormat + '\'' +
                ", annotationsEnabled=" + annotationsEnabled +
                ", annotationsMode="    + annotationsMode +
                ", htmlEscaping="       + htmlEscaping +
                '}';
    }

    /**
     * Defines how annotations should be processed.
     */
    public enum AnnotationsMode {

        /**
         * Process only Obsidian-specific annotations.
         */
        OBSIDIAN_ONLY,

        /**
         * Process both Obsidian and Gson annotations.
         */
        OBSIDIAN_AND_GSON,

        /**
         * Disable all annotation processing.
         */
        NONE

    }

    /**
     * Builder class for constructing a {@link JsonConfig} instance.
     * Provides various configuration options such as enabling pretty-printing,
     * serializing null values, setting a custom date format, enabling/disabling
     * annotation processing, and configuring handling of unknown fields or HTML escaping.
     */
    public static final class Builder {

        private boolean         prettyPrint         = false;
        private boolean         serializeNulls      = false;
        private boolean         lenient             = false;
        private boolean         failOnUnknownFields = false;
        private boolean         annotationsEnabled  = true;
        private boolean         htmlEscaping        = true;
        private String          dateFormat          = null;
        private AnnotationsMode annotationsMode     = AnnotationsMode.OBSIDIAN_ONLY;

        private Builder() {}

        /**
         * Enables or disables pretty-printing of JSON output.
         *
         * @param prettyPrint true to enable pretty-printing
         * @return this builder
         */
        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        /**
         * Enables or disables serialization of null values.
         *
         * @param serializeNulls true to serialize nulls
         * @return this builder
         */
        public Builder serializeNulls(boolean serializeNulls) {
            this.serializeNulls = serializeNulls;
            return this;
        }

        /**
         * Enables or disables lenient parsing (allows malformed JSON).
         *
         * @param lenient true to enable lenient parsing
         * @return this builder
         */
        public Builder lenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        /**
         * Enables or disables failing on unknown fields during deserialization.
         *
         * @param failOnUnknownFields true to fail on unknown fields
         * @return this builder
         */
        public Builder failOnUnknownFields(boolean failOnUnknownFields) {
            this.failOnUnknownFields = failOnUnknownFields;
            return this;
        }

        /**
         * Sets the date format pattern for date serialization.
         *
         * @param dateFormat the date format pattern (e.g., "yyyy-MM-dd")
         * @return this builder
         */
        public Builder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        /**
         * Enables or disables annotation processing.
         *
         * @param enabled true to enable annotations
         * @return this builder
         */
        public Builder enableAnnotations(boolean enabled) {
            this.annotationsEnabled = enabled;
            return this;
        }

        /**
         * Sets the annotation processing mode.
         *
         * @param mode the annotations mode
         * @return this builder
         * @throws IllegalArgumentException if mode is null
         */
        public Builder annotationsMode(AnnotationsMode mode) {
            if (mode == null) {
                throw new IllegalArgumentException("Annotations mode cannot be null");
            }
            this.annotationsMode = mode;
            return this;
        }

        /**
         * Enables or disables HTML escaping in JSON strings.
         *
         * @param htmlEscaping true to enable HTML escaping
         * @return this builder
         */
        public Builder htmlEscaping(boolean htmlEscaping) {
            this.htmlEscaping = htmlEscaping;
            return this;
        }

        /**
         * Builds an immutable JsonConfig instance.
         *
         * @return the configuration instance
         */
        @Contract(value = " -> new", pure = true)
        public @NotNull JsonConfig build() {
            return new JsonConfig(this);
        }

        /**
         * Creates a JsonMapper with this configuration.
         *
         * @return a new JSON mapper
         */
        public @NotNull JsonMapper buildMapper() {
            return Json.mapper(build());
        }
    }
}