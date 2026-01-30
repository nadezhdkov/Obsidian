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

package io.obsidian.json.internal.gson;

import io.obsidian.json.api.*;
import json.api.*;
import obsidian.json.api.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Bridge for converting between Gson and Obsidian JSON elements.
 *
 * <p>This class handles the bidirectional conversion between Gson's
 * JsonElement hierarchy and Obsidian's JsonElement hierarchy.</p>
 *
 * @since 1.0.0
 */
public final class GsonElementBridge {

    private GsonElementBridge() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts a Gson JsonElement to an Obsidian JsonElement.
     *
     * @param gsonElement the Gson element
     * @return the Obsidian element
     */
    public static JsonElement toObsidian(com.google.gson.JsonElement gsonElement) {
        if (gsonElement == null || gsonElement.isJsonNull()) {
            return JsonNull.INSTANCE;
        }

        if (gsonElement.isJsonObject()) {
            return toObsidianObject(gsonElement.getAsJsonObject());
        }

        if (gsonElement.isJsonArray()) {
            return toObsidianArray(gsonElement.getAsJsonArray());
        }

        if (gsonElement.isJsonPrimitive()) {
            return toObsidianPrimitive(gsonElement.getAsJsonPrimitive());
        }

        throw new IllegalArgumentException("Unknown Gson element type: " + gsonElement.getClass());
    }

    /**
     * Converts an Obsidian JsonElement to a Gson JsonElement.
     *
     * @param obsidianElement the Obsidian element
     * @return the Gson element
     */
    public static com.google.gson.JsonElement toGson(JsonElement obsidianElement) {
        if (obsidianElement == null || obsidianElement.isJsonNull()) {
            return com.google.gson.JsonNull.INSTANCE;
        }

        if (obsidianElement.isJsonObject()) {
            return toGsonObject(obsidianElement.asJsonObject());
        }

        if (obsidianElement.isJsonArray()) {
            return toGsonArray(obsidianElement.asJsonArray());
        }

        if (obsidianElement.isJsonPrimitive()) {
            return toGsonPrimitive(obsidianElement.asJsonPrimitive());
        }

        throw new IllegalArgumentException("Unknown Obsidian element type: " + obsidianElement.getClass());
    }

    private static @NotNull JsonObject toObsidianObject(com.google.gson.@NotNull JsonObject gsonObject) {
        JsonObject result = new JsonObject();
        for (var entry : gsonObject.entrySet()) {
            result.add(entry.getKey(), toObsidian(entry.getValue()));
        }
        return result;
    }

    private static @NotNull JsonArray toObsidianArray(com.google.gson.@NotNull JsonArray gsonArray) {
        JsonArray result = new JsonArray();
        for (com.google.gson.JsonElement element : gsonArray) {
            result.add(toObsidian(element));
        }
        return result;
    }

    @Contract("_ -> new")
    private static @NotNull JsonPrimitive toObsidianPrimitive(com.google.gson.@NotNull JsonPrimitive gsonPrimitive) {
        if (gsonPrimitive.isBoolean()) {
            return new JsonPrimitive(gsonPrimitive.getAsBoolean());
        }
        if (gsonPrimitive.isNumber()) {
            return new JsonPrimitive(gsonPrimitive.getAsNumber());
        }
        if (gsonPrimitive.isString()) {
            return new JsonPrimitive(gsonPrimitive.getAsString());
        }
        throw new IllegalArgumentException("Unknown primitive type");
    }

    private static com.google.gson.@NotNull JsonObject toGsonObject(@NotNull JsonObject obsidianObject) {
        com.google.gson.JsonObject result = new com.google.gson.JsonObject();
        for (var entry : obsidianObject.entrySet()) {
            result.add(entry.getKey(), toGson(entry.getValue()));
        }
        return result;
    }

    private static com.google.gson.@NotNull JsonArray toGsonArray(@NotNull JsonArray obsidianArray) {
        com.google.gson.JsonArray result = new com.google.gson.JsonArray();
        for (JsonElement element : obsidianArray) {
            result.add(toGson(element));
        }
        return result;
    }

    @Contract("_ -> new")
    private static com.google.gson.@NotNull JsonPrimitive toGsonPrimitive(@NotNull JsonPrimitive obsidianPrimitive) {
        if (obsidianPrimitive.isBoolean()) {
            return new com.google.gson.JsonPrimitive(obsidianPrimitive.getAsBoolean());
        }
        if (obsidianPrimitive.isNumber()) {
            return new com.google.gson.JsonPrimitive(obsidianPrimitive.getAsNumber());
        }
        if (obsidianPrimitive.isString()) {
            return new com.google.gson.JsonPrimitive(obsidianPrimitive.asString());
        }
        throw new IllegalArgumentException("Unknown primitive type");
    }
}