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

import io.obsidian.file.exception.FileOperationException;
import io.obsidian.file.exception.FileWriteException;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Fluent directory operations: create, list, clean, and recursive delete.
 *
 * <p>All failure modes throw typed {@link FileOperationException} subtypes
 * instead of generic {@code RuntimeException}.</p>
 *
 * @since 1.0
 */
public class Directory {

    @Getter
    private final Path path;

    private Directory(String dirPath) {
        this.path = Paths.get(dirPath);
    }

    @Contract("_ -> new")
    public static @NotNull Directory at(@NotNull String dirPath) {
        return new Directory(Objects.requireNonNull(dirPath));
    }

    @Contract("_ -> new")
    public static @NotNull Directory at(@NotNull Path path) {
        return new Directory(Objects.requireNonNull(path).toString());
    }

    /**
     * Creates the directory (and parents) if it doesn't exist.
     *
     * @return this instance for fluent chaining
     * @throws FileWriteException if creation fails
     */
    public Directory create() {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new FileWriteException("Failed to create directory: " + path, e, path);
            }
        }
        return this;
    }

    public boolean exists() {
        return Files.exists(path) && Files.isDirectory(path);
    }

    /**
     * Checks whether the directory is empty.
     *
     * @return {@code true} if the directory has no children
     * @throws FileWriteException if listing fails
     */
    public boolean isEmpty() {
        if (!exists()) return true;
        try (var entries = Files.list(path)) {
            return entries.findAny().isEmpty();
        } catch (IOException e) {
            throw new FileWriteException("Failed to list directory: " + path, e, path);
        }
    }

    /**
     * Lists all direct children of the directory.
     *
     * @return list of child paths
     * @throws FileWriteException if listing fails
     */
    public List<Path> list() {
        try (var stream = Files.list(path)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileWriteException("Failed to list directory: " + path, e, path);
        }
    }

    /**
     * Lists all direct children names of the directory.
     *
     * @return list of child file/directory names
     * @throws FileWriteException if listing fails
     */
    public List<String> listNames() {
        try (var stream = Files.list(path)) {
            return stream
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileWriteException("Failed to list directory names: " + path, e, path);
        }
    }

    /**
     * Deletes the directory and all its contents recursively.
     *
     * @return this instance for fluent chaining
     * @throws FileWriteException if deletion fails
     */
    public Directory deleteRecursively() {
        if (Files.notExists(path)) return this;

        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(this::deletePathUnchecked);
        } catch (IOException e) {
            throw new FileWriteException("Failed to delete directory recursively: " + path, e, path);
        }
        return this;
    }

    /**
     * Removes all contents of the directory without deleting the directory itself.
     *
     * @return this instance for fluent chaining
     * @throws FileWriteException if cleaning fails
     */
    public Directory clean() {
        if (Files.notExists(path)) return this;

        try (var stream = Files.list(path)) {
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    Directory.at(p).deleteRecursively();
                } else {
                    deletePathUnchecked(p);
                }
            });
        } catch (IOException e) {
            throw new FileWriteException("Failed to clean directory: " + path, e, path);
        }
        return this;
    }

    private void deletePathUnchecked(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
