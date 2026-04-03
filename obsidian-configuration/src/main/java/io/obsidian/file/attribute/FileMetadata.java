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

package io.obsidian.file.attribute;

import io.obsidian.file.exception.FileReadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

/**
 * Immutable snapshot of a file's metadata, captured atomically from the filesystem.
 *
 * <p>All attributes are read in a single pass via {@link BasicFileAttributes} to
 * avoid TOCTOU races and minimize disk I/O. Use {@link #of(Path)} to produce a
 * consistent snapshot.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * var meta = FileMetadata.of(Path.of("config.yml"));
 * System.out.println(meta.sizeFormatted()); // "1.42 KB"
 * System.out.println(meta.extension());     // "yml"
 * }</pre>
 *
 * @param fileName       the file name including extension (e.g. {@code "config.yml"})
 * @param extension      the file extension without dot (e.g. {@code "yml"}), empty string if none
 * @param absolutePath   the fully resolved absolute path
 * @param sizeBytes      file size in bytes
 * @param createdAt      creation timestamp, may be {@code null} on filesystems that don't track it
 * @param lastModified   last modification timestamp
 * @param owner          file owner name, may be {@code null} if unavailable
 * @param mimeType       probed MIME type, may be {@code null} if undetectable
 * @param isRegularFile  {@code true} if the path points to a regular file
 * @param isDirectory    {@code true} if the path points to a directory
 * @param isSymbolicLink {@code true} if the path is a symbolic link
 * @param isHidden       {@code true} if the file is hidden
 * @since 1.0
 */
public record FileMetadata(
        @NotNull String fileName,
        @NotNull String extension,
        @NotNull String absolutePath,
        long sizeBytes,
        @Nullable Instant createdAt,
        @NotNull Instant lastModified,
        @Nullable String owner,
        @Nullable String mimeType,
        boolean isRegularFile,
        boolean isDirectory,
        boolean isSymbolicLink,
        boolean isHidden
) {

    /**
     * Creates an atomic snapshot of the file's metadata.
     *
     * <p>Reads {@link BasicFileAttributes} in one call, then augments with owner
     * and MIME type. This design ensures the core attributes (size, timestamps,
     * file type flags) are consistent with each other.</p>
     *
     * @param path the file path to capture metadata from
     * @return an immutable {@code FileMetadata} record
     * @throws FileReadException if the filesystem cannot be queried
     */
    public static @NotNull FileMetadata of(@NotNull Path path) {
        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class);
            var resolved = path.toAbsolutePath().normalize();
            var name = path.getFileName() != null ? path.getFileName().toString() : "";

            return new FileMetadata(
                    name,
                    extractExtension(name),
                    resolved.toString(),
                    attrs.size(),
                    attrs.creationTime().toInstant(),
                    attrs.lastModifiedTime().toInstant(),
                    resolveOwner(path),
                    resolveMimeType(path),
                    attrs.isRegularFile(),
                    attrs.isDirectory(),
                    attrs.isSymbolicLink(),
                    resolveHidden(path)
            );
        } catch (IOException e) {
            throw new FileReadException("Failed to read metadata for: " + path, e, path);
        }
    }

    /**
     * Returns the file size formatted in human-readable units (B, KB, MB, GB).
     *
     * @return formatted size string
     */
    public @NotNull String sizeFormatted() {
        if (sizeBytes < 1024L) return sizeBytes + " B";
        if (sizeBytes < 1024L * 1024) return String.format("%.2f KB", sizeBytes / 1024.0);
        if (sizeBytes < 1024L * 1024 * 1024) return String.format("%.2f MB", sizeBytes / (1024.0 * 1024));
        return String.format("%.2f GB", sizeBytes / (1024.0 * 1024 * 1024));
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private static @NotNull String extractExtension(@NotNull String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0 && dot < fileName.length() - 1) ? fileName.substring(dot + 1) : "";
    }

    private static @Nullable String resolveOwner(@NotNull Path path) {
        try {
            return Files.getOwner(path).getName();
        } catch (IOException | UnsupportedOperationException e) {
            return null;
        }
    }

    private static @Nullable String resolveMimeType(@NotNull Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean resolveHidden(@NotNull Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }
}
