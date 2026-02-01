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

package obsidian.json.api.codec;

import obsidian.json.error.JsonMappingException;
import obsidian.json.api.JsonElement;

/**
 * Custom serializer/deserializer for specific types.
 *
 * <p>Codecs provide a way to customize how specific types are converted
 * to and from JSON. They are useful when:</p>
 * <ul>
 *   <li>Default serialization doesn't meet your needs</li>
 *   <li>Working with third-party types you can't annotate</li>
 *   <li>Implementing domain-specific conversion logic</li>
 * </ul>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * public class UuidCodec implements JsonCodec<UUID> {
 *
 *     @Override
 *     public JsonElement encode(UUID value) {
 *         return new JsonPrimitive(value.toString());
 *     }
 *
 *     @Override
 *     public UUID decode(JsonElement element) {
 *         return UUID.fromString(element.asJsonPrimitive().asString());
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type this codec handles
 * @since 1.0.0
 */
public interface JsonCodec<T> {

    /**
     * Encodes a Java object into a JsonElement.
     *
     * @param value the object to encode
     * @return the JSON representation
     * @throws JsonMappingException if encoding fails
     */
    JsonElement encode(T value);

    /**
     * Decodes a JsonElement into a Java object.
     *
     * @param element the JSON element to decode
     * @return the decoded object
     * @throws JsonMappingException if decoding fails
     * @throws IllegalArgumentException if element is null
     */
    T decode(JsonElement element);
}