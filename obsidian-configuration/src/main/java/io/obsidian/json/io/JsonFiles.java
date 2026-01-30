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

package io.obsidian.json.io;

import io.obsidian.json.api.Json;
import io.obsidian.json.api.JsonElement;
import io.obsidian.json.api.JsonMapper;
import io.obsidian.json.api.codec.TypeReference;
import io.obsidian.json.error.JsonIoException;
import io.obsidian.json.error.JsonMappingException;
import io.obsidian.json.error.JsonParseException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class for reading and writing JSON files.
 *
 * <p>This class provides convenient methods for file-based JSON operations,
 * handling I/O concerns separately from parsing and mapping.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Read a JSON file
 * User user = JsonFiles.read(
 *     Paths.get("user.json"),
 *     TypeRef.of(User.class)
 * );
 *
 * // Write to a JSON file
 * JsonFiles.write(Paths.get("user.json"), user);
 *
 * // Read as JsonElement
 * JsonElement element = JsonFiles.parse(Paths.get("data.json"));
 * }</pre>
 *
 * @since 1.0.0
 */
public final class JsonFiles {

    private static final JsonMapper DEFAULT_MAPPER = Json.defaultMapper();

    private JsonFiles() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Reads and decodes a JSON file into a Java object.
     *
     * @param <T> the target type
     * @param path the file path
     * @param type the type reference
     * @return the decoded object
     * @throws JsonIoException if file I/O fails
     * @throws JsonParseException if parsing fails
     * @throws JsonMappingException if mapping fails
     * @throws IllegalArgumentException if path or type is null
     */
    public static <T> T read(Path path, TypeReference<T> type) {
        return read(path, type, DEFAULT_MAPPER);
    }

    /**
     * Reads and decodes a JSON file using a custom mapper.
     *
     * @param <T> the target type
     * @param path the file path
     * @param type the type reference
     * @param mapper the JSON mapper to use
     * @return the decoded object
     * @throws JsonIoException if file I/O fails
     * @throws JsonParseException if parsing fails
     * @throws JsonMappingException if mapping fails
     * @throws IllegalArgumentException if any parameter is null
     */
    public static <T> T read(Path path, TypeReference<T> type, JsonMapper mapper) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type reference cannot be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper cannot be null");
        }
        return mapper.decode(JsonSource.of(path), type);
    }

    /**
     * Parses a JSON file into a JsonElement tree.
     *
     * @param path the file path
     * @return the parsed JSON element
     * @throws JsonIoException if file I/O fails
     * @throws JsonParseException if parsing fails
     * @throws IllegalArgumentException if path is null
     */
    public static JsonElement parse(Path path) {
        return parse(path, DEFAULT_MAPPER);
    }

    /**
     * Parses a JSON file using a custom mapper.
     *
     * @param path the file path
     * @param mapper the JSON mapper to use
     * @return the parsed JSON element
     * @throws JsonIoException if file I/O fails
     * @throws JsonParseException if parsing fails
     * @throws IllegalArgumentException if path or mapper is null
     */
    public static JsonElement parse(Path path, JsonMapper mapper) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper cannot be null");
        }
        return mapper.parse(JsonSource.of(path));
    }

    /**
     * Writes an object to a JSON file.
     *
     * @param path the file path
     * @param value the object to write
     * @throws JsonIoException if file I/O fails
     * @throws JsonMappingException if encoding fails
     * @throws IllegalArgumentException if path is null
     */
    public static void write(Path path, Object value) {
        write(path, value, DEFAULT_MAPPER);
    }

    /**
     * Writes an object to a JSON file using a custom mapper.
     *
     * @param path the file path
     * @param value the object to write
     * @param mapper the JSON mapper to use
     * @throws JsonIoException if file I/O fails
     * @throws JsonMappingException if encoding fails
     * @throws IllegalArgumentException if path or mapper is null
     */
    public static void write(Path path, Object value, JsonMapper mapper) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper cannot be null");
        }

        JsonElement element = mapper.encode(value);
        String json = mapper.stringify(element);

        try {
            java.nio.file.Files.writeString(path, json, JsonCharset.getDefault());
        } catch (IOException e) {
            throw new JsonIoException(
                    "Failed to write file: " + path, e
            );
        }
    }

    /**
     * Writes a JsonElement to a JSON file.
     *
     * @param path the file path
     * @param element the JSON element to write
     * @throws JsonIoException if file I/O fails
     * @throws IllegalArgumentException if path or element is null
     */
    public static void write(Path path, JsonElement element) {
        write(path, element, DEFAULT_MAPPER);
    }

    /**
     * Writes a JsonElement to a JSON file using a custom mapper.
     *
     * @param path the file path
     * @param element the JSON element to write
     * @param mapper the JSON mapper to use
     * @throws JsonIoException if file I/O fails
     * @throws IllegalArgumentException if any parameter is null
     */
    public static void write(Path path, JsonElement element, JsonMapper mapper) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper cannot be null");
        }

        String json = mapper.stringify(element);

        try {
            java.nio.file.Files.writeString(path, json, JsonCharset.getDefault());
        } catch (IOException e) {
            throw new JsonIoException(
                    "Failed to write file: " + path, e
            );
        }
    }
}