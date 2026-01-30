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

/**
 * Abstract base class for all JSON elements.
 *
 * <p>This class represents any valid JSON value in the JSON tree model.
 * A JSON element can be:</p>
 * <ul>
 *   <li>{@link JsonObject} - a JSON object (key-value pairs)</li>
 *   <li>{@link JsonArray} - a JSON array (ordered list)</li>
 *   <li>{@link JsonPrimitive} - a primitive value (string, number, boolean)</li>
 *   <li>{@link JsonNull} - the JSON null value</li>
 * </ul>
 *
 * <p>This design provides a type-safe way to work with JSON data while
 * maintaining the flexibility of the JSON format.</p>
 *
 * @since 1.0.0
 */
public abstract class JsonElement {

    /**
     * Checks if this element is a JSON object.
     *
     * @return true if this is a JsonObject
     */
    public boolean isJsonObject() {
        return this instanceof JsonObject;
    }

    /**
     * Checks if this element is a JSON array.
     *
     * @return true if this is a JsonArray
     */
    public boolean isJsonArray() {
        return this instanceof JsonArray;
    }

    /**
     * Checks if this element is a JSON primitive.
     *
     * @return true if this is a JsonPrimitive
     */
    public boolean isJsonPrimitive() {
        return this instanceof JsonPrimitive;
    }

    /**
     * Checks if this element is JSON null.
     *
     * @return true if this is JsonNull
     */
    public boolean isJsonNull() {
        return this instanceof JsonNull;
    }

    /**
     * Converts this element to a JsonObject.
     *
     * @return this element as a JsonObject
     * @throws IllegalStateException if this is not a JsonObject
     */
    public JsonObject asJsonObject() {
        if (isJsonObject()) {
            return (JsonObject) this;
        }
        throw new IllegalStateException("Not a JSON Object: " + this);
    }

    /**
     * Converts this element to a JsonArray.
     *
     * @return this element as a JsonArray
     * @throws IllegalStateException if this is not a JsonArray
     */
    public JsonArray asJsonArray() {
        if (isJsonArray()) {
            return (JsonArray) this;
        }
        throw new IllegalStateException("Not a JSON Array: " + this);
    }

    /**
     * Converts this element to a JsonPrimitive.
     *
     * @return this element as a JsonPrimitive
     * @throws IllegalStateException if this is not a JsonPrimitive
     */
    public JsonPrimitive asJsonPrimitive() {
        if (isJsonPrimitive()) {
            return (JsonPrimitive) this;
        }
        throw new IllegalStateException("Not a JSON Primitive: " + this);
    }

    /**
     * Creates a deep copy of this JSON element.
     *
     * @return a deep copy of this element
     */
    public abstract JsonElement deepCopy();
}