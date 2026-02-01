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

import obsidian.json.api.codec.TypeReference;
import obsidian.json.error.JsonIoException;
import obsidian.json.error.JsonMappingException;
import obsidian.json.error.JsonParseException;
import obsidian.json.error.JsonValidationException;
import obsidian.json.io.JsonSource;

/**
 * Core contract for JSON mapping operations.
 *
 * <p>This interface provides methods for parsing, encoding, decoding, and
 * stringifying JSON data. All implementations must be thread-safe.</p>
 *
 * <p>The mapper supports two main workflows:</p>
 * <ul>
 *   <li><b>Direct mapping:</b> from source/object to object/string</li>
 *   <li><b>Element-based:</b> through intermediate JsonElement representation</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface JsonMapper {

    /**
     * Parses JSON from a source into a JsonElement tree.
     *
     * <p>This method converts raw JSON data into an intermediate representation
     * that can be navigated and manipulated programmatically.</p>
     *
     * @param source the JSON source to parse
     * @return the parsed JSON element tree
     * @throws JsonParseException if parsing fails
     * @throws JsonIoException if I/O errors occur
     * @throws IllegalArgumentException if source is null
     */
    JsonElement parse(JsonSource source);

    /**
     * Decodes JSON from a source directly into a Java object.
     *
     * <p>This is a convenience method that combines parsing and mapping in a
     * single operation.</p>
     *
     * @param <T> the target type
     * @param source the JSON source to decode
     * @param type the type reference for the target object
     * @return the decoded Java object
     * @throws JsonParseException if parsing fails
     * @throws JsonMappingException if mapping fails
     * @throws JsonValidationException if validation fails
     * @throws JsonIoException if I/O errors occur
     * @throws IllegalArgumentException if source or type is null
     */
    <T> T decode(JsonSource source, TypeReference<T> type);

    /**
     * Decodes a JsonElement into a Java object.
     *
     * <p>Use this method when you already have a parsed JsonElement and want
     * to convert it to a specific type.</p>
     *
     * @param <T> the target type
     * @param element the JSON element to decode
     * @param type the type reference for the target object
     * @return the decoded Java object
     * @throws JsonMappingException if mapping fails
     * @throws JsonValidationException if validation fails
     * @throws IllegalArgumentException if element or type is null
     */
    <T> T decode(JsonElement element, TypeReference<T> type);

    /**
     * Encodes a Java object into a JsonElement tree.
     *
     * @param value the object to encode (may be null)
     * @return the encoded JSON element
     * @throws JsonMappingException if encoding fails
     */
    JsonElement encode(Object value);

    /**
     * Converts a JsonElement to its JSON string representation.
     *
     * <p>The output format (pretty-printed or compact) depends on the
     * mapper's configuration.</p>
     *
     * @param element the element to stringify
     * @return the JSON string representation
     * @throws JsonMappingException if stringification fails
     * @throws IllegalArgumentException if element is null
     */
    String stringify(JsonElement element);
}