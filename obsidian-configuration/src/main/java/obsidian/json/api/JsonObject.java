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

package obsidian.json.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a JSON object (a collection of key-value pairs).
 *
 * <p>This class provides methods to add, get, and remove properties from
 * the JSON object. The order of properties is preserved.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * JsonObject obj = new JsonObject();
 * obj.addProperty("name", "John");
 * obj.addProperty("age", 30);
 * obj.add("address", addressObject);
 *
 * String name = obj.get("name").asJsonPrimitive().asString();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class JsonObject extends JsonElement {

    private final Map<String, JsonElement> members = new LinkedHashMap<>();

    /**
     * Creates an empty JSON object.
     */
    public JsonObject() {}

    /**
     * Adds a property with a JsonElement value.
     *
     * @param property the property name
     * @param value the value (null will add JsonNull)
     * @throws IllegalArgumentException if property is null
     */
    public void add(String property, JsonElement value) {
        if (property == null) {
            throw new IllegalArgumentException("Property name cannot be null");
        }
        members.put(property, value == null ? JsonNull.INSTANCE : value);
    }

    /**
     * Adds a property with a String value.
     *
     * @param property the property name
     * @param value the string value
     * @throws IllegalArgumentException if property is null
     */
    public void addProperty(String property, String value) {
        add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds a property with a Number value.
     *
     * @param property the property name
     * @param value the number value
     * @throws IllegalArgumentException if property is null
     */
    public void addProperty(String property, Number value) {
        add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds a property with a Boolean value.
     *
     * @param property the property name
     * @param value the boolean value
     * @throws IllegalArgumentException if property is null
     */
    public void addProperty(String property, Boolean value) {
        add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds a property with a Character value.
     *
     * @param property the property name
     * @param value the character value
     * @throws IllegalArgumentException if property is null
     */
    public void addProperty(String property, Character value) {
        add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Removes a property from this object.
     *
     * @param property the property name to remove
     * @return the removed element, or null if it didn't exist
     */
    public JsonElement remove(String property) {
        return members.remove(property);
    }

    /**
     * Gets a property value from this object.
     *
     * @param property the property name
     * @return the value, or null if the property doesn't exist
     */
    public JsonElement get(String property) {
        return members.get(property);
    }

    /**
     * Gets a primitive property as JsonPrimitive.
     *
     * @param property the property name
     * @return the primitive value, or null if not found
     * @throws IllegalStateException if the value exists but is not a primitive
     */
    public @Nullable JsonPrimitive getAsJsonPrimitive(String property) {
        JsonElement element = get(property);
        return element != null ? element.asJsonPrimitive() : null;
    }

    /**
     * Gets an object property as JsonObject.
     *
     * @param property the property name
     * @return the object value, or null if not found
     * @throws IllegalStateException if the value exists but is not an object
     */
    public @Nullable JsonObject getAsJsonObject(String property) {
        JsonElement element = get(property);
        return element != null ? element.asJsonObject() : null;
    }

    /**
     * Gets an array property as JsonArray.
     *
     * @param property the property name
     * @return the array value, or null if not found
     * @throws IllegalStateException if the value exists but is not an array
     */
    public @Nullable JsonArray getAsJsonArray(String property) {
        JsonElement element = get(property);
        return element != null ? element.asJsonArray() : null;
    }

    /**
     * Checks if this object contains a property.
     *
     * @param property the property name
     * @return true if the property exists
     */
    public boolean has(String property) {
        return members.containsKey(property);
    }

    /**
     * Returns a set of all property names.
     *
     * @return set of property names
     */
    @Contract(pure = true)
    public @NotNull Set<String> keySet() {
        return members.keySet();
    }

    /**
     * Returns a set of all entries in this object.
     *
     * @return set of entries
     */
    @Contract(pure = true)
    public @NotNull Set<Map.Entry<String, JsonElement>> entrySet() {
        return members.entrySet();
    }

    /**
     * Returns the number of properties in this object.
     *
     * @return the size
     */
    public int size() {
        return members.size();
    }

    /**
     * Checks if this object is empty.
     *
     * @return true if there are no properties
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public @NotNull JsonElement deepCopy() {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
            result.add(entry.getKey(), entry.getValue().deepCopy());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof JsonObject &&
                ((JsonObject) o).members.equals(members));
    }

    @Override
    public int hashCode() {
        return members.hashCode();
    }
}