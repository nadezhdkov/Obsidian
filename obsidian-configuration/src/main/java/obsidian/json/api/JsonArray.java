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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a JSON array (an ordered list of JSON elements).
 *
 * <p>This class provides methods to add, get, and iterate over elements
 * in the array. Elements can be of any JsonElement type.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * JsonArray array = new JsonArray();
 * array.add("value1");
 * array.add(42);
 * array.add(true);
 *
 * for (JsonElement element : array) {
 *     System.out.println(element);
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public final class JsonArray extends JsonElement implements Iterable<JsonElement> {

    private final List<JsonElement> elements = new ArrayList<>();

    /**
     * Creates an empty JSON array.
     */
    public JsonArray() {}

    /**
     * Adds a JsonElement to this array.
     *
     * @param element the element to add (null will add JsonNull)
     */
    public void add(JsonElement element) {
        elements.add(element == null ? JsonNull.INSTANCE : element);
    }

    /**
     * Adds a String value to this array.
     *
     * @param value the string value
     */
    public void add(String value) {
        add(value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds a Number value to this array.
     *
     * @param value the number value
     */
    public void add(Number value) {
        add(value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds a Boolean value to this array.
     *
     * @param value the boolean value
     */
    public void add(Boolean value) {
        add(value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds a Character value to this array.
     *
     * @param value the character value
     */
    public void add(Character value) {
        add(value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
    }

    /**
     * Adds all elements from another array to this array.
     *
     * @param array the array to add elements from
     * @throws IllegalArgumentException if array is null
     */
    public void addAll(JsonArray array) {
        if (array == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        elements.addAll(array.elements);
    }

    /**
     * Sets the element at the specified index.
     *
     * @param index the index
     * @param element the element to set
     * @return the previous element at that index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public JsonElement set(int index, JsonElement element) {
        return elements.set(index, element == null ? JsonNull.INSTANCE : element);
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index the index
     * @return the removed element
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public JsonElement remove(int index) {
        return elements.remove(index);
    }

    /**
     * Removes the first occurrence of the specified element.
     *
     * @param element the element to remove
     * @return true if the element was found and removed
     */
    public boolean remove(JsonElement element) {
        return elements.remove(element);
    }

    /**
     * Checks if this array contains the specified element.
     *
     * @param element the element to check
     * @return true if the element is in the array
     */
    public boolean contains(JsonElement element) {
        return elements.contains(element);
    }

    /**
     * Gets the element at the specified index.
     *
     * @param index the index
     * @return the element at that index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public JsonElement get(int index) {
        return elements.get(index);
    }

    /**
     * Returns the number of elements in this array.
     *
     * @return the size
     */
    public int size() {
        return elements.size();
    }

    /**
     * Checks if this array is empty.
     *
     * @return true if there are no elements
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Returns an iterator over the elements in this array.
     *
     * @return an iterator
     */
    @Override
    public @NotNull Iterator<JsonElement> iterator() {
        return elements.iterator();
    }

    @Override
    public @NotNull JsonElement deepCopy() {
        JsonArray result = new JsonArray();
        for (JsonElement element : elements) {
            result.add(element.deepCopy());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof JsonArray &&
                ((JsonArray) o).elements.equals(elements));
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}