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

package io.obsidian.file.operation;

import io.obsidian.file.exception.FileWriteException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;

/**
 * Encapsulates filesystem lifecycle and manipulation operations:
 * create, delete, copy, move, rename, backup, and symbolic/hard links.
 *
 * <p>All mutating operations throw {@link FileWriteException} with the affected
 * {@link Path} on failure.</p>
 *
 * @since 1.0
 */
public final class FileOperations {

    private final Path path;

    public FileOperations(@NotNull Path path) {
        this.path = path;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    /**
     * Creates the file and all necessary parent directories if they don't exist.
     *
     * @throws FileWriteException if creation fails
     */
    public void createIfNotExists() {
        if (Files.notExists(path)) {
            try {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            } catch (IOException e) {
                throw new FileWriteException("Failed to create file: " + path, e, path);
            }
        }
    }

    /**
     * Creates parent directories without creating the file itself.
     *
     * @throws FileWriteException if directory creation fails
     */
    public void createDirectoriesIfNeeded() {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new FileWriteException("Failed to create directories for: " + path, e, path);
            }
        }
    }

    /**
     * Creates directories and the file itself.
     *
     * @throws FileWriteException if any creation step fails
     */
    public void create() {
        createDirectoriesIfNeeded();
        createIfNotExists();
    }

    /**
     * Deletes the file if it exists.
     *
     * @throws FileWriteException if deletion fails
     */
    public void delete() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileWriteException("Failed to delete file: " + path, e, path);
        }
    }

    /**
     * Deletes the file only if the given condition is met.
     *
     * @param condition predicate evaluated against the path
     * @throws FileWriteException if deletion fails
     */
    public void deleteIf(@NotNull Predicate<Path> condition) {
        if (condition.test(path)) {
            delete();
        }
    }

    // ── Manipulation ──────────────────────────────────────────────────────

    /**
     * Copies the file to the target path, replacing if it already exists.
     *
     * @param targetPath the destination path
     * @throws FileWriteException if the copy fails
     */
    public void copyTo(@NotNull String targetPath) {
        try {
            Files.copy(path, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileWriteException("Failed to copy " + path + " to " + targetPath, e, path);
        }
    }

    /**
     * Copies the file to the target path only if the target does not exist.
     *
     * @param targetPath the destination path
     * @throws FileWriteException if the copy fails
     */
    public void copyToIfNotExists(@NotNull String targetPath) {
        var target = Paths.get(targetPath);
        if (Files.notExists(target)) {
            copyTo(targetPath);
        }
    }

    /**
     * Moves the file to the target path, replacing if it already exists.
     *
     * @param targetPath the destination path
     * @throws FileWriteException if the move fails
     */
    public void moveTo(@NotNull String targetPath) {
        try {
            Files.move(path, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileWriteException("Failed to move " + path + " to " + targetPath, e, path);
        }
    }

    /**
     * Renames the file within its parent directory.
     *
     * @param newFileName the new file name (not a full path)
     * @return the new resolved {@link Path}
     * @throws FileWriteException if the rename fails
     */
    public @NotNull Path renameTo(@NotNull String newFileName) {
        var parent  = path.getParent();
        var newPath = (parent != null) ? parent.resolve(newFileName) : Paths.get(newFileName);
        try {
            Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
            return newPath;
        } catch (IOException e) {
            throw new FileWriteException("Failed to rename " + path + " to " + newFileName, e, path);
        }
    }

    /**
     * Creates a timestamped backup copy in the same directory.
     * The backup name follows the pattern: {@code filename.timestamp.bak}
     *
     * @throws FileWriteException if the backup copy fails
     */
    public void backup() {
        var fileName   = path.getFileName() != null ? path.getFileName().toString() : "unknown";
        var backupName = fileName + "." + System.currentTimeMillis() + ".bak";
        var target     = path.getParent() != null
                ? path.getParent().resolve(backupName).toString()
                : backupName;
        copyTo(target);
    }

    // ── Links ─────────────────────────────────────────────────────────────

    /**
     * Creates a hard link pointing to this file.
     *
     * @param linkPath the path for the new hard link
     * @throws FileWriteException if link creation fails
     */
    public void createHardLink(@NotNull String linkPath) {
        try {
            Files.createLink(Paths.get(linkPath), path);
        } catch (IOException e) {
            throw new FileWriteException("Failed to create hard link at " + linkPath, e, path);
        }
    }

    /**
     * Creates a symbolic link pointing to this file.
     *
     * @param linkPath the path for the new symbolic link
     * @throws FileWriteException if link creation fails
     */
    public void createSymbolicLink(@NotNull String linkPath) {
        try {
            Files.createSymbolicLink(Paths.get(linkPath), path);
        } catch (IOException e) {
            throw new FileWriteException("Failed to create symbolic link at " + linkPath, e, path);
        }
    }

    // ── Temporary Files ───────────────────────────────────────────────────

    /**
     * Creates a temporary file with the given prefix and suffix.
     *
     * @param prefix the temp file prefix
     * @param suffix the temp file suffix (e.g. {@code ".tmp"})
     * @return the path of the newly created temp file
     * @throws FileWriteException if creation fails
     */
    public static @NotNull Path createTemp(@NotNull String prefix, @NotNull String suffix) {
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new FileWriteException("Failed to create temp file", e, null);
        }
    }

    /**
     * Checks whether two paths refer to the same file on the filesystem.
     *
     * @param path1 first path
     * @param path2 second path
     * @return {@code true} if both paths resolve to the same file
     */
    public static boolean isSameFile(@NotNull String path1, @NotNull String path2) {
        try {
            return Files.isSameFile(Paths.get(path1), Paths.get(path2));
        } catch (IOException e) {
            return false;
        }
    }
}
