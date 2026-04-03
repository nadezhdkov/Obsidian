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

import io.obsidian.file.exception.FileWriteException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Manages file permission flags and POSIX permission sets.
 *
 * <p>Methods that manage POSIX-specific permissions will throw
 * {@link UnsupportedOperationException} on non-POSIX filesystems (e.g. Windows NTFS).</p>
 *
 * @since 1.0
 */
public final class FilePermissions {

    private final Path path;

    public FilePermissions(@NotNull Path path) {
        this.path = path;
    }

    /**
     * Returns the file owner name.
     *
     * @return the owner name
     * @throws FileWriteException if the owner cannot be read
     */
    public @NotNull String getOwner() {
        try {
            return Files.getOwner(path).getName();
        } catch (IOException e) {
            throw new FileWriteException("Failed to read owner: " + path, e, path);
        }
    }

    /**
     * Sets the file owner.
     *
     * @param owner the new owner name
     * @throws FileWriteException if the owner cannot be set
     */
    public void setOwner(@NotNull String owner) {
        try {
            var user = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(owner);
            Files.setOwner(path, user);
        } catch (IOException e) {
            throw new FileWriteException("Failed to set owner to '" + owner + "': " + path, e, path);
        }
    }

    /**
     * Returns the POSIX file permissions.
     *
     * @return the set of POSIX permissions
     * @throws UnsupportedOperationException on non-POSIX filesystems
     */
    public @NotNull Set<PosixFilePermission> getPosixPermissions() {
        try {
            return Files.getPosixFilePermissions(path);
        } catch (IOException e) {
            throw new FileWriteException("Failed to read POSIX permissions: " + path, e, path);
        }
    }

    /**
     * Sets the POSIX file permissions.
     *
     * @param permissions the permission set to apply
     * @throws UnsupportedOperationException on non-POSIX filesystems
     */
    public void setPosixPermissions(@NotNull Set<PosixFilePermission> permissions) {
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (IOException e) {
            throw new FileWriteException("Failed to set POSIX permissions: " + path, e, path);
        }
    }

    /**
     * Makes the file read-only (removes write and execute flags).
     */
    public void setReadOnly() {
        var file = path.toFile();
        file.setWritable(false);
        file.setExecutable(false);
    }

    /**
     * Makes the file writable.
     */
    public void setWritable() {
        path.toFile().setWritable(true);
    }

    /**
     * Makes the file executable.
     */
    public void setExecutable() {
        path.toFile().setExecutable(true);
    }
}
