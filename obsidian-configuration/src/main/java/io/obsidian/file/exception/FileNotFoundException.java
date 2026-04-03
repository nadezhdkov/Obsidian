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
 * Thrown when a file or directory expected to exist is not found.
 *
 * <p>This is distinct from {@link java.io.FileNotFoundException} — it belongs to
 * the Obsidian sealed exception hierarchy and carries the {@link Path} context.</p>
 *
 * @since 1.0
 */
public final class FileNotFoundException extends FileOperationException {

    public FileNotFoundException(String message, Throwable cause, Path path) {
        super(message, cause, path);
    }

    public FileNotFoundException(Path path) {
        super("File not found: " + path, null, path);
    }

    public FileNotFoundException(Path path, Throwable cause) {
        super("File not found: " + path, cause, path);
    }
}
