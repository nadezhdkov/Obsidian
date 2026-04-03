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

package io.obsidian.file;

import io.obsidian.file.attribute.FileMetadata;
import io.obsidian.file.hash.FileHasher;
import io.obsidian.file.hash.HashAlgorithm;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Facade entry-point for the Obsidian file module.
 *
 * <p>Provides static convenience methods for the most common operations without
 * requiring the caller to navigate the internal package structure. All methods
 * delegate to the appropriate component.</p>
 *
 * <h3>Quick Reference</h3>
 * <pre>{@code
 * // Fluent file operations
 * File.at("config.yml").create().write("key: value");
 *
 * // Directory operations
 * File.directory("data/").create();
 *
 * // One-shot reads
 * String content = File.read("config.yml");
 *
 * // Hashing with Strategy pattern
 * String hash = File.hash("data.bin", new Sha256Hash());
 *
 * // Atomic metadata snapshot
 * FileMetadata meta = File.metadata("config.yml");
 * }</pre>
 *
 * @since 1.0
 */
public interface File {

    /**
     * Creates a {@link FileHandle} for the specified file path.
     *
     * @param filePath the path of the file
     * @return a fluent {@code FileHandle} instance
     */
    @Contract("_ -> new")
    static @NotNull FileHandle at(@NotNull String filePath) {
        return FileHandle.at(filePath);
    }

    /**
     * Creates a {@link FileHandle} for the specified {@link Path}.
     *
     * @param path the file path
     * @return a fluent {@code FileHandle} instance
     */
    @Contract("_ -> new")
    static @NotNull FileHandle at(@NotNull Path path) {
        return FileHandle.at(path);
    }

    /**
     * Creates a {@link Directory} for the specified directory path.
     *
     * @param dirPath the directory path
     * @return a fluent {@code Directory} instance
     */
    @Contract("_ -> new")
    static @NotNull Directory directory(@NotNull String dirPath) {
        return Directory.at(dirPath);
    }

    /**
     * Retrieves a {@link Path} for the specified string representation.
     *
     * @param path the string path
     * @return a {@code Path} object
     */
    static @NotNull Path get(@NotNull String path) {
        return Paths.get(path);
    }

    /**
     * Combines path segments into a single normalized {@link Path}.
     *
     * @param first the initial path segment
     * @param more  additional segments
     * @return the combined path
     */
    static @NotNull Path combine(@NotNull String first, @NotNull String... more) {
        return Paths.get(first, more);
    }

    /**
     * Converts the given path to an absolute, normalized form.
     *
     * @param path the path
     * @return the absolute normalized path
     */
    static @NotNull Path toAbsolutePath(@NotNull Path path) {
        return path.toAbsolutePath().normalize();
    }

    /**
     * Reads the entire content of a file as a string.
     *
     * @param filePath the file path
     * @return the file content
     */
    static @NotNull String read(@NotNull String filePath) {
        return FileHandle.at(filePath).readAllText();
    }

    /**
     * Reads the entire content of a file as a byte array.
     *
     * @param filePath the file path
     * @return the raw bytes
     */
    static byte @NotNull [] readBytes(@NotNull String filePath) {
        return FileHandle.at(filePath).readAllBytes();
    }

    /**
     * Writes content to a file, creating it if necessary.
     *
     * @param filePath the file path
     * @param content  the content to write
     */
    static void write(@NotNull String filePath, @NotNull String content) {
        FileHandle.at(filePath).write(content);
    }

    /**
     * Writes content to a file, optionally appending.
     *
     * @param filePath the file path
     * @param content  the content to write
     * @param append   {@code true} to append, {@code false} to truncate
     */
    static void write(@NotNull String filePath, @NotNull String content, boolean append) {
        FileHandle.at(filePath).write(content, append);
    }

    /**
     * Computes a file hash using a JCA algorithm name.
     *
     * @param filePath  the file path
     * @param algorithm the algorithm name (e.g. {@code "SHA-256"})
     * @return the hex-encoded hash
     */
    static @NotNull String getFileHash(@NotNull String filePath, @NotNull String algorithm) {
        return FileHasher.hash(Paths.get(filePath), algorithm);
    }

    /**
     * Computes a file hash using a typed {@link HashAlgorithm} strategy.
     *
     * @param filePath  the file path
     * @param algorithm the hashing strategy
     * @return the hex-encoded hash
     */
    static @NotNull String hash(@NotNull String filePath, @NotNull HashAlgorithm algorithm) {
        return FileHasher.hash(Paths.get(filePath), algorithm);
    }

    /**
     * Captures an atomic metadata snapshot for the given file.
     *
     * @param filePath the file path
     * @return an immutable {@link FileMetadata} record
     */
    static @NotNull FileMetadata metadata(@NotNull String filePath) {
        return FileMetadata.of(Paths.get(filePath));
    }

    /**
     * Probes the MIME type for the given file path.
     *
     * @param filePath the file path
     * @return the MIME type, or {@code null} if undetermined
     */
    static @Nullable String getMimeType(@NotNull String filePath) {
        return FileHandle.getMimeType(filePath);
    }
}
