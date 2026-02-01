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

package obsidian.json.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Charset configuration for JSON I/O operations.
 *
 * <p>JSON text is always encoded in UTF-8 per the JSON specification (RFC 8259).
 * This class provides the standard charset and utility methods for encoding.</p>
 *
 * @since 1.0.0
 */
public final class JsonCharset {

    /**
     * The standard charset for JSON (UTF-8).
     */
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    private JsonCharset() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the default charset for JSON operations.
     *
     * @return UTF-8 charset
     */
    public static Charset getDefault() {
        return UTF_8;
    }
}
