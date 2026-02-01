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

package obsidian.json.io;

import obsidian.json.error.JsonIoException;
import lombok.Getter;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abstraction for JSON output destinations.
 *
 * <p>This class provides a unified way to represent different destinations
 * for JSON data (writers, files) without exposing implementation details.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // To writer
 * JsonSink sink = JsonSink.of(new FileWriter("output.json"));
 *
 * // To path
 * JsonSink sink = JsonSink.of(Paths.get("config.json"));
 * }</pre>
 *
 * @since 1.0.0
 */
public final class JsonSink {

    @Getter
    private final Path     path;

    private final Writer   writer;

    private final SinkType type;

    private enum SinkType {
        WRITER, PATH
    }

    private JsonSink(Writer writer) {
        this.writer = writer;
        this.path   = null;
        this.type   = SinkType.WRITER;
    }

    private JsonSink(Path path) {
        this.writer = null;
        this.path   = path;
        this.type   = SinkType.PATH;
    }

    /**
     * Creates a sink from a Writer.
     *
     * @param writer the writer
     * @return a JSON sink
     * @throws IllegalArgumentException if writer is null
     */
    public static JsonSink of(Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("Writer cannot be null");
        }
        return new JsonSink(writer);
    }

    /**
     * Creates a sink from a file path.
     *
     * @param path the file path
     * @return a JSON sink
     * @throws IllegalArgumentException if path is null
     */
    public static JsonSink of(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return new JsonSink(path);
    }

    /**
     * Gets this sink as a Writer.
     *
     * <p>For internal use by the implementation.</p>
     *
     * @return a writer for this sink
     * @throws JsonIoException if I/O errors occur
     */
    public Writer asWriter() {
        switch (type) {
            case WRITER:
                return writer;
            case PATH:
                try {
                    return Files.newBufferedWriter(path, JsonCharset.getDefault());
                } catch (Exception e) {
                    throw new JsonIoException(
                            "Failed to open file for writing: " + path, e
                    );
                }
            default:
                throw new IllegalStateException("Unknown sink type");
        }
    }

    /**
     * Checks if this is a writer sink.
     *
     * @return true if writer sink
     */
    public boolean isWriter() {
        return type == SinkType.WRITER;
    }

    /**
     * Checks if this is a path sink.
     *
     * @return true if path sink
     */
    public boolean isPath() {
        return type == SinkType.PATH;
    }

    @Override
    public String toString() {
        return switch (type) {
            case WRITER -> "JsonSink{writer=" + writer.getClass().getSimpleName() + "}";
            case PATH -> "JsonSink{path=" + path + "}";
            default -> "JsonSink{unknown}";
        };
    }
}