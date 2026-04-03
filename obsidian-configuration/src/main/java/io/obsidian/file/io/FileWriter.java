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

package io.obsidian.file.io;

import io.obsidian.file.exception.FileWriteException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Encapsulates all write operations on a single file path.
 *
 * <p>Every method throws {@link FileWriteException} on I/O failure, providing
 * the affected {@link Path} in the exception context.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * var writer = new FileWriter(Path.of("output.txt"), StandardCharsets.UTF_8);
 * writer.write("Hello, Obsidian!");
 * writer.append("\nSecond line");
 * }</pre>
 *
 * @since 1.0
 */
public final class FileWriter {

    private final Path    path;
    private final Charset charset;

    /**
     * Creates a writer bound to the given path and charset.
     *
     * @param path    the file to write to
     * @param charset the charset to use for text encoding
     */
    public FileWriter(@NotNull Path path, @NotNull Charset charset) {
        this.path    = path;
        this.charset = charset;
    }

    /**
     * Writes content to the file, optionally appending.
     *
     * @param content the text to write
     * @param append  {@code true} to append, {@code false} to truncate
     * @throws FileWriteException if the write operation fails
     */
    public void write(@NotNull String content, boolean append) {
        var mode = append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
        try {
            Files.writeString(path, content, charset, StandardOpenOption.CREATE, mode);
        } catch (IOException e) {
            throw new FileWriteException(path, e);
        }
    }

    /**
     * Writes content to the file, truncating any existing content.
     *
     * @param content the text to write
     * @throws FileWriteException if the write operation fails
     */
    public void write(@NotNull String content) {
        write(content, false);
    }

    /**
     * Appends content to the end of the file.
     *
     * @param content the text to append
     * @throws FileWriteException if the write operation fails
     */
    public void append(@NotNull String content) {
        write(content, true);
    }

    /**
     * Writes a list of lines to the file (truncating existing content).
     *
     * @param lines the lines to write
     * @throws FileWriteException if the write operation fails
     */
    public void writeLines(@NotNull List<String> lines) {
        try {
            Files.write(path, lines, charset);
        } catch (IOException e) {
            throw new FileWriteException(path, e);
        }
    }

    /**
     * Writes raw bytes to the file (truncating existing content).
     *
     * @param bytes the bytes to write
     * @throws FileWriteException if the write operation fails
     */
    public void writeBytes(byte @NotNull [] bytes) {
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new FileWriteException(path, e);
        }
    }

    /**
     * Serializes a Java object to the file using {@link ObjectOutputStream}.
     *
     * @param object the serializable object to persist
     * @throws FileWriteException if serialization or the write fails
     */
    public void writeObject(@NotNull Serializable object) {
        try (var os  = Files.newOutputStream(path);
             var oos = new ObjectOutputStream(os)) {
            oos.writeObject(object);
        } catch (IOException e) {
            throw new FileWriteException("Failed to serialize object to: " + path, e, path);
        }
    }

    /**
     * Clears the file content by writing an empty string.
     *
     * @throws FileWriteException if the write operation fails
     */
    public void clear() {
        write("");
    }
}
