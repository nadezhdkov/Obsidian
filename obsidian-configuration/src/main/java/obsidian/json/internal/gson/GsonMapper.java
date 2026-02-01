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
import com.google.gson.JsonSyntaxException;
import obsidian.json.api.JsonConfig;
import obsidian.json.api.JsonElement;
import obsidian.json.api.JsonMapper;
import obsidian.json.api.codec.TypeReference;
import obsidian.json.error.JsonIoException;
import obsidian.json.error.JsonMappingException;
import obsidian.json.error.JsonParseException;
import obsidian.json.io.JsonSource;
import obsidian.json.util.JsonPrettyPrinter;

import java.io.IOException;
import java.io.Reader;

/**
 * Internal Gson-based implementation of JsonMapper.
 *
 * <p>This class wraps Google Gson and adapts it to the Obsidian JSON API.
 * It is NOT part of the public API and should not be used directly.</p>
 *
 * @since 1.0.0
 */
public final class GsonMapper implements JsonMapper {

    private final Gson gson;
    private final JsonConfig config;
    private final GsonEngine engine;

    /**
     * Creates a new GsonMapper with the specified configuration.
     *
     * @param config the configuration
     */
    public GsonMapper(JsonConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.config = config;
        this.engine = new GsonEngine(config);
        this.gson = engine.createGson();
    }

    @Override
    public JsonElement parse(JsonSource source) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        try (Reader reader = source.asReader()) {
            com.google.gson.JsonElement gsonElement = gson.fromJson(reader, com.google.gson.JsonElement.class);
            return GsonElementBridge.toObsidian(gsonElement);
        } catch (JsonSyntaxException e) {
            throw new JsonParseException("Failed to parse JSON", e);
        } catch (IOException e) {
            throw new JsonIoException("I/O error while reading JSON", e);
        } catch (Exception e) {
            throw new JsonParseException("Unexpected error during parsing", e);
        }
    }

    @Override
    public <T> T decode(JsonSource source, TypeReference<T> type) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type reference cannot be null");
        }

        try (Reader reader = source.asReader()) {
            return gson.fromJson(reader, type.getType());
        } catch (JsonSyntaxException e) {
            throw new JsonParseException("Failed to parse JSON", e);
        } catch (IOException e) {
            throw new JsonIoException("I/O error while reading JSON", e);
        } catch (Exception e) {
            throw new JsonMappingException("Failed to decode JSON to " + type.getType(), e);
        }
    }

    @Override
    public <T> T decode(JsonElement element, TypeReference<T> type) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type reference cannot be null");
        }

        try {
            com.google.gson.JsonElement gsonElement = GsonElementBridge.toGson(element);
            return gson.fromJson(gsonElement, type.getType());
        } catch (Exception e) {
            throw new JsonMappingException("Failed to decode JSON element to " + type.getType(), e);
        }
    }

    @Override
    public JsonElement encode(Object value) {
        try {
            com.google.gson.JsonElement gsonElement = gson.toJsonTree(value);
            return GsonElementBridge.toObsidian(gsonElement);
        } catch (Exception e) {
            throw new JsonMappingException("Failed to encode object to JSON", e);
        }
    }

    @Override
    public String stringify(JsonElement element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }

        try {
            if (config.isPrettyPrint()) {
                return JsonPrettyPrinter.toPrettyString(element);
            } else {
                return JsonPrettyPrinter.toCompactString(element);
            }
        } catch (Exception e) {
            throw new JsonMappingException("Failed to stringify JSON element", e);
        }
    }
}