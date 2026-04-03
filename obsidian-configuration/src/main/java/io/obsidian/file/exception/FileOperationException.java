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

package io.obsidian.file.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Base exception for all file operations in the Obsidian file module.
 *
 * <p>This is a {@code sealed} hierarchy that provides typed, contextual exceptions
 * for every category of file operation, replacing the generic {@code RuntimeException}
 * wrapping pattern. Each subtype carries the {@link Path} that triggered the failure.</p>
 *
 * <h3>Hierarchy</h3>
 * <pre>
 * FileOperationException (sealed)
 * ├── FileReadException
 * ├── FileWriteException
 * ├── FileNotFoundException
 * ├── FileCompressionException
 * └── FileHashException
 * </pre>
 *
 * @since 1.0
 */
public sealed class FileOperationException extends RuntimeException
        permits FileReadException, FileWriteException,
                FileNotFoundException, FileCompressionException,
                FileHashException {

    private final @Nullable Path targetPath;

    /**
     * Constructs a new exception with a contextual message, root cause, and the affected path.
     *
     * @param message descriptive message explaining why the operation failed
     * @param cause   the underlying I/O or system exception
     * @param path    the filesystem path involved in the failed operation, may be {@code null}
     */
    protected FileOperationException(@NotNull String message, @Nullable Throwable cause, @Nullable Path path) {
        super(message, cause);
        this.targetPath = path;
    }

    /**
     * Returns the filesystem path that caused this exception.
     *
     * @return the affected path, or {@code null} if not available
     */
    public @Nullable Path getTargetPath() {
        return targetPath;
    }
}
