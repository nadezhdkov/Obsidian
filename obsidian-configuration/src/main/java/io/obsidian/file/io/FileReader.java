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

import io.obsidian.file.exception.FileReadException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates all read operations on a single file path.
 *
 * <p>Every method throws {@link FileReadException} on I/O failure, providing
 * the affected {@link Path} in the exception context. Stream-returning methods
 * such as {@link #lines()} must be closed by the caller.</p>
 *
 * @since 1.0
 */
public final class FileReader {

    private final Path    path;
    private final Charset charset;

    /**
     * Creates a reader bound to the given path and charset.
     *
     * @param path    the file to read from
     * @param charset the charset to use for text decoding
     */
    public FileReader(@NotNull Path path, @NotNull Charset charset) {
        this.path    = path;
        this.charset = charset;
    }

    /**
     * Reads the entire file content as a single string.
     *
     * @return the file content
     * @throws FileReadException if the file cannot be read
     */
    public @NotNull String readAllText() {
        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            throw new FileReadException(path, e);
        }
    }

    /**
     * Reads the entire file content as a byte array.
     *
     * @return raw bytes
     * @throws FileReadException if the file cannot be read
     */
    public byte @NotNull [] readAllBytes() {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FileReadException(path, e);
        }
    }

    /**
     * Reads all lines of the file into a list.
     *
     * @return a list of lines
     * @throws FileReadException if the file cannot be read
     */
    public @NotNull List<String> readAllLines() {
        try {
            return Files.readAllLines(path, charset);
        } catch (IOException e) {
            throw new FileReadException(path, e);
        }
    }

    /**
     * Returns a lazy {@link Stream} of lines. The caller <strong>must</strong>
     * close the returned stream (e.g. via try-with-resources).
     *
     * @return a line stream backed by the file
     * @throws FileReadException if the file cannot be opened
     */
    public @NotNull Stream<String> lines() {
        try {
            return Files.lines(path, charset);
        } catch (IOException e) {
            throw new FileReadException(path, e);
        }
    }

    /**
     * Deserializes a Java object from the file.
     *
     * @param type the expected class of the deserialized object
     * @param <T>  the target type
     * @return the deserialized object
     * @throws FileReadException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public <T> T readObject(@NotNull Class<T> type) {
        try (var is  = Files.newInputStream(path);
             var ois = new ObjectInputStream(is)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new FileReadException("Failed to deserialize object from: " + path, e, path);
        }
    }

    /**
     * Reads the first {@code n} lines of the file.
     *
     * @param n the number of lines to read
     * @return a list containing at most {@code n} lines
     * @throws FileReadException if the file cannot be read
     */
    public @NotNull List<String> readFirstLines(int n) {
        try (var stream = lines()) {
            return stream.limit(n).collect(Collectors.toList());
        }
    }

    /**
     * Reads the last {@code n} lines of the file.
     *
     * <p>This loads all lines into memory. For very large files, consider
     * a streaming tail implementation.</p>
     *
     * @param n the number of trailing lines to read
     * @return a list containing at most {@code n} lines from the end
     * @throws FileReadException if the file cannot be read
     */
    public @NotNull List<String> readLastLines(int n) {
        var allLines = readAllLines();
        int size = allLines.size();
        return allLines.subList(Math.max(0, size - n), size);
    }
}
