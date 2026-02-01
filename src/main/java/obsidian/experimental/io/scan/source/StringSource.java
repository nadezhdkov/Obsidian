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

package obsidian.experimental.io.scan.source;

import java.io.*;

/**
 * {@link InputSource} backed by an in-memory {@link String}.
 *
 * <h2>Overview</h2>
 * {@code StringSource} allows input scanning from static text content.
 *
 * <p>
 * This implementation is primarily intended for:
 * <ul>
 *   <li>Testing</li>
 *   <li>Scripted input</li>
 *   <li>Batch processing</li>
 *   <li>Simulated console interaction</li>
 * </ul>
 *
 * <h2>Null handling</h2>
 * If a {@code null} string is provided, it is treated as an empty input.
 *
 * <h2>Resource management</h2>
 * The underlying {@link StringReader} is closed when {@link #close()} is called.
 *
 * @see InputSource
 */
public final class StringSource implements InputSource {

    private final BufferedReader reader;

    /**
     * Creates a new input source from the given string content.
     *
     * @param content the input content; {@code null} is treated as an empty string.
     */
    public StringSource(String content) {
        this.reader = new BufferedReader(new StringReader(content == null ? "" : content));
    }

    @Override
    public BufferedReader reader() {
        return reader;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException ignored) {}
    }
}