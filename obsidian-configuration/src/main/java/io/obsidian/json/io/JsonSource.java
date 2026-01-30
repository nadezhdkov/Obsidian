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

import io.obsidian.json.error.JsonIoException;
import lombok.Getter;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

/**
 * Abstraction for JSON input sources.
 *
 * <p>This class provides a unified way to represent different sources
 * of JSON data (strings, readers, files) without exposing implementation
 * details in the public API.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // From string
 * JsonSource source = JsonSource.of("{\"name\":\"John\"}");
 *
 * // From reader
 * JsonSource source = JsonSource.of(new FileReader("data.json"));
 *
 * // From path
 * JsonSource source = JsonSource.of(Paths.get("config.json"));
 * }</pre>
 *
 * @since 1.0.0
 */
public final class JsonSource {

    @Getter
    private final String     string;

    private final Reader     reader;

    @Getter
    private final Path       path;

    private final SourceType type;

    private enum SourceType {
        STRING, READER, PATH
    }

    private JsonSource(String string) {
        this.string = string;
        this.reader = null;
        this.path   = null;
        this.type   = SourceType.STRING;
    }

    private JsonSource(Reader reader) {
        this.string = null;
        this.reader = reader;
        this.path = null;
        this.type = SourceType.READER;
    }

    private JsonSource(Path path) {
        this.string = null;
        this.reader = null;
        this.path   = path;
        this.type   = SourceType.PATH;
    }

    /**
     * Creates a source from a JSON string.
     *
     * @param json the JSON string
     * @return a JSON source
     * @throws IllegalArgumentException if json is null
     */
    public static JsonSource of(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON string cannot be null");
        }
        return new JsonSource(json);
    }

    /**
     * Creates a source from a Reader.
     *
     * @param reader the reader
     * @return a JSON source
     * @throws IllegalArgumentException if reader is null
     */
    public static JsonSource of(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Reader cannot be null");
        }
        return new JsonSource(reader);
    }

    /**
     * Creates a source from a file path.
     *
     * @param path the file path
     * @return a JSON source
     * @throws IllegalArgumentException if path is null
     */
    public static JsonSource of(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return new JsonSource(path);
    }

    /**
     * Gets this source as a Reader.
     *
     * <p>For internal use by the implementation.</p>
     *
     * @return a reader for this source
     * @throws JsonIoException if I/O errors occur
     */
    public Reader asReader() {
        switch (type) {
            case STRING:
                return new StringReader(string);

            case READER:
                return reader;

            case PATH:
                try {
                    return java.nio.file.Files.newBufferedReader(path, JsonCharset.getDefault());
                } catch (Exception e) {
                    throw new JsonIoException(
                            "Failed to open file: " + path, e
                    );
                }

            default:
                throw new IllegalStateException("Unknown source type");
        }
    }

    /**
     * Checks if this is a string source.
     *
     * @return true if string source
     */
    public boolean isString() {
        return type == SourceType.STRING;
    }

    /**
     * Checks if this is a reader source.
     *
     * @return true if reader source
     */
    public boolean isReader() {
        return type == SourceType.READER;
    }

    /**
     * Checks if this is a path source.
     *
     * @return true if path source
     */
    public boolean isPath() {
        return type == SourceType.PATH;
    }

    @Override
    public String toString() {
        return switch (type) {
            case STRING -> "JsonSource{string=" + (string.length() > 50
                    ? string.substring(0, 50) + "..."
                    : string) + "}";

            case READER -> "JsonSource{reader=" + reader.getClass().getSimpleName() + "}";

            case PATH -> "JsonSource{path=" + path + "}";

            default -> "JsonSource{unknown}";
        };
    }
}