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

import java.nio.file.Path;

/**
 * Thrown when a read operation on the filesystem fails.
 *
 * <p>Covers text reads, byte reads, line streams, object deserialization,
 * and any other operation that consumes data from a file.</p>
 *
 * @since 1.0
 */
public final class FileReadException extends FileOperationException {

    public FileReadException(String message, Throwable cause, Path path) {
        super(message, cause, path);
    }

    public FileReadException(Path path, Throwable cause) {
        super("Failed to read file: " + path, cause, path);
    }
}
