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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import obsidian.json.api.JsonConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Internal engine for creating and configuring Gson instances.
 *
 * <p>This class translates Obsidian JsonConfig into Gson configuration.
 * It is NOT part of the public API.</p>
 *
 * @since 1.0.0
 */
public final class GsonEngine {

    private final JsonConfig config;

    public GsonEngine(JsonConfig config) {
        this.config = config;
    }

    /**
     * Creates a configured Gson instance.
     *
     * @return the Gson instance
     */
    public @NotNull Gson createGson() {
        GsonBuilder builder = new GsonBuilder();

        if (config.isPrettyPrint()) {
            builder.setPrettyPrinting();
        }

        if (config.isSerializeNulls()) {
            builder.serializeNulls();
        }

        if (config.isLenient()) {
            builder.setLenient();
        }

        if (!config.isHtmlEscaping()) {
            builder.disableHtmlEscaping();
        }

        if (config.getDateFormat() != null) {
            builder.setDateFormat(config.getDateFormat());
        }

        if (config.isAnnotationsEnabled()) {
            GsonAnnotationProcessor processor = new GsonAnnotationProcessor(config);
            processor.configure(builder);
        }

        return builder.create();
    }
}