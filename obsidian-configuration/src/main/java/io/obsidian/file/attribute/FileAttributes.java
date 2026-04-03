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
import java.nio.file.attribute.FileTime;
import java.time.Instant;

/**
 * Provides individual attribute queries and produces atomic {@link FileMetadata} snapshots.
 *
 * <p>For single-attribute access (e.g. just the size), the individual getters avoid
 * the overhead of reading all attributes. For multiple attributes, prefer
 * {@link #snapshot()} which reads them atomically.</p>
 *
 * @since 1.0
 */
public final class FileAttributes {

    private final Path path;

    public FileAttributes(@NotNull Path path) {
        this.path = path;
    }

    /**
     * Captures an atomic snapshot of all file metadata.
     *
     * @return an immutable {@link FileMetadata} record
     * @throws FileReadException if the filesystem cannot be queried
     */
    public @NotNull FileMetadata snapshot() {
        return FileMetadata.of(path);
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public long size() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new FileReadException("Failed to read size of: " + path, e, path);
        }
    }

    /**
     * Returns the file size formatted in human-readable units.
     *
     * @return formatted size string (e.g. {@code "1.42 KB"})
     */
    public @NotNull String sizeFormatted() {
        long s = size();
        if (s < 1024L) return s + " B";
        if (s < 1024L * 1024) return String.format("%.2f KB", s / 1024.0);
        if (s < 1024L * 1024 * 1024) return String.format("%.2f MB", s / (1024.0 * 1024));
        return String.format("%.2f GB", s / (1024.0 * 1024 * 1024));
    }

    public boolean isRegularFile() { return Files.isRegularFile(path); }
    public boolean isDirectory()   { return Files.isDirectory(path); }
    public boolean isSymbolicLink(){ return Files.isSymbolicLink(path); }
    public boolean isReadable()    { return Files.isReadable(path); }
    public boolean isWritable()    { return Files.isWritable(path); }
    public boolean isExecutable()  { return Files.isExecutable(path); }

    public boolean isHidden() {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    public @NotNull String getFileName() {
        return path.getFileName() != null ? path.getFileName().toString() : "";
    }

    public @NotNull String getFileNameWithoutExtension() {
        var name = getFileName();
        int dot  = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    public @NotNull String getExtension() {
        return extractExtension(getFileName());
    }

    public @NotNull String getAbsolutePath() {
        return path.toAbsolutePath().normalize().toString();
    }

    public @Nullable String getParent() {
        var parent = path.getParent();
        return parent != null ? parent.toString() : null;
    }

    public @NotNull Instant getLastModifiedTime() {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            throw new FileReadException("Failed to read last modified time: " + path, e, path);
        }
    }

    public void setLastModifiedTime(@NotNull Instant instant) {
        try {
            Files.setLastModifiedTime(path, FileTime.from(instant));
        } catch (IOException e) {
            throw new FileReadException("Failed to set last modified time: " + path, e, path);
        }
    }

    public @NotNull Instant getCreationTime() {
        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.creationTime().toInstant();
        } catch (IOException e) {
            throw new FileReadException("Failed to read creation time: " + path, e, path);
        }
    }

    public @Nullable String getOwner() {
        try {
            return Files.getOwner(path).getName();
        } catch (IOException | UnsupportedOperationException e) {
            return null;
        }
    }

    public static @NotNull String extractExtension(@NotNull String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index > 0 && index < fileName.length() - 1) ? fileName.substring(index + 1) : "";
    }

    public static @Nullable String getMimeType(@NotNull Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            return null;
        }
    }
}
