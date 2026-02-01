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
 * {@link InputSource} backed by a {@link Reader}.
 *
 * <h2>Overview</h2>
 * {@code ReaderSource} allows the scanning system to consume input from any
 * {@link Reader} implementation.
 *
 * <p>
 * This is especially useful for:
 * <ul>
 *   <li>File-based input</li>
 *   <li>Network streams</li>
 *   <li>Unit tests</li>
 *   <li>Custom pipelines</li>
 * </ul>
 *
 * <h2>Resource management</h2>
 * The provided reader is wrapped in a {@link BufferedReader}.
 * When {@link #close()} is called, the underlying reader is closed.
 *
 * @see InputSource
 */
public final class ReaderSource implements InputSource {

    private final BufferedReader reader;

    /**
     * Creates a new input source backed by the given reader.
     *
     * @param reader the reader used as input source; must not be {@code null}.
     */
    public ReaderSource(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    @Override
    public BufferedReader reader() {
        return reader;
    }
    @Override public void close() {
        try {
            reader.close();
        } catch (IOException ignored) {}
    }
}