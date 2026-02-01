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

package obsidian.experimental.io.scan.error;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an error encountered during the scanning or tokenization process.
 * <p>
 * This class captures the error category, a descriptive message, the exact
 * location in the input stream (offset, line, column), and an optional code
 * snippet to provide context for debugging.
 */
public final class Error {

    public final ErrorCode     type;
    public final String        message;
    public final long          offset;
    public final int           line;
    public final int           column;
    public final String        snippet;
    public final Throwable     cause;

    /**
     * Constructs a new ScanError with full diagnostic information.
     *
     * @param type    the category of the error.
     * @param message a human-readable description of the error.
     * @param offset  the absolute character offset in the input.
     * @param line    the line number where the error occurred (1-indexed).
     * @param column  the column number where the error occurred (1-indexed).
     * @param snippet a small portion of the input text around the error location.
     * @param cause   the underlying exception that triggered this error, if any.
     */
    public Error(
            ErrorCode type,
            String        message,
            long          offset,
            int           line,
            int           column,
            String        snippet,
            Throwable     cause
    ) {
        this.type    = type;
        this.message = message;
        this.offset  = offset;
        this.line    = line;
        this.column  = column;
        this.snippet = snippet;
        this.cause   = cause;
    }

    /**
     * Formats the error into a human-readable string suitable for logs or consoles.
     * <p>
     * Example output:
     * <pre>
     * [INVALID_INT] Invalid int: 'abc' (line 5, col 12)
     * > var x = abc;
     * </pre>
     *
     * @return a formatted error message.
     */
    @Contract(pure = true)
    public @NotNull String pretty() {
        String pos  = "line " + line + ", col " + column;
        String snip = (snippet == null || snippet.isBlank()) ? "" : ("\n  > " + snippet);
        return "[" + type + "] " + message + " (" + pos + ")" + snip;
    }
}