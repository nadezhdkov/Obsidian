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

import java.io.BufferedReader;

/**
 * Abstraction over an input origin used by the scanning system.
 *
 * <h2>Overview</h2>
 * {@code InputSource} represents a unified way to read textual input regardless
 * of where it comes from.
 *
 * <p>
 * Implementations may provide input from:
 * <ul>
 *   <li>Console ({@link ConsoleSource})</li>
 *   <li>{@link java.io.Reader} ({@link ReaderSource})</li>
 *   <li>In-memory strings ({@link StringSource})</li>
 * </ul>
 *
 * <p>
 * The scanner engine interacts only with this interface, allowing input sources
 * to be swapped without changing parsing or validation logic.
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li>Decouple input origin from parsing logic</li>
 *   <li>Support testing via string-based sources</li>
 *   <li>Enable custom input implementations</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * Implementations are responsible for managing their underlying resources.
 * The {@link #close()} method is invoked when the scanner engine is closed.
 *
 * @see ConsoleSource
 * @see ReaderSource
 * @see StringSource
 */
public interface InputSource extends AutoCloseable {

    /**
     * Returns the {@link BufferedReader} used to read input.
     *
     * <p>
     * The returned reader must remain valid for the lifetime of the scanner.
     *
     * @return the buffered reader providing input data.
     */
    BufferedReader reader();

    /**
     * Closes the underlying input source and releases any resources.
     */
    @Override
    void close();
}
