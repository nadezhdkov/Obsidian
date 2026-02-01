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

package obsidian.json.internal.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;
import obsidian.json.annotations.*;
import obsidian.json.api.JsonConfig;
import obsidian.json.api.codec.JsonCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Processes Obsidian annotations and configures Gson accordingly.
 *
 * <p>This class handles:</p>
 * <ul>
 *   <li>@JsonName - field naming</li>
 *   <li>@JsonIgnore - field exclusion</li>
 *   <li>@JsonAdapter - custom codecs</li>
 *   <li>@JsonRequired - validation (runtime)</li>
 *   <li>@JsonDefault - default values (runtime)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class GsonAnnotationProcessor {

    private final JsonConfig config;

    public GsonAnnotationProcessor(JsonConfig config) {
        this.config = config;
    }

    /**
     * Configures a GsonBuilder with annotation processing.
     *
     * @param builder the Gson builder
     */
    public void configure(@NotNull GsonBuilder builder) {
        builder.setFieldNamingStrategy             (new ObsidianFieldNamingStrategy());
        builder.addSerializationExclusionStrategy  (new IgnoreExclusionStrategy());
        builder.addDeserializationExclusionStrategy(new IgnoreExclusionStrategy());
    }

    /**
     * Field naming strategy that respects @JsonName annotation.
     */
    private static class ObsidianFieldNamingStrategy implements FieldNamingStrategy {

        @Override
        public String translateName(@NotNull Field field) {
            JsonName annotation = field.getAnnotation(JsonName.class);
            if (annotation != null) {
                return annotation.value();
            }
            return field.getName();
        }
    }

    /**
     * Exclusion strategy that respects @JsonIgnore annotation.
     */
    private static class IgnoreExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(@NotNull FieldAttributes field) {
            return field.getAnnotation(JsonIgnore.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

    /**
     * Checks if a field has a custom codec via @JsonAdapter.
     *
     * @param field the field to check
     * @return the codec instance, or null
     */
    public static @Nullable JsonCodec<?> getCodecForField(@NotNull Field field) {
        JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
        if (annotation != null) {
            try {
                Class<? extends JsonCodec<?>> codecClass = annotation.value();
                return codecClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate codec for field: " + field.getName(), e);
            }
        }
        return null;
    }

    /**
     * Checks if a field is required via @JsonRequired.
     *
     * @param field the field
     * @return true if required
     */
    @Contract(pure = true)
    public static boolean isRequired(@NotNull Field field) {
        return field.getAnnotation(JsonRequired.class) != null;
    }

    /**
     * Gets the default value for a field via @JsonDefault.
     *
     * @param field the field
     * @return the default value string, or null
     */
    public static @Nullable String getDefaultValue(@NotNull Field field) {
        JsonDefault annotation = field.getAnnotation(JsonDefault.class);
        return annotation != null ? annotation.value() : null;
    }
}