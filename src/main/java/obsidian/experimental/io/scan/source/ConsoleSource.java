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
import java.io.InputStreamReader;

/**
 * {@link InputSource} implementation backed by {@link System#in}.
 *
 * <h2>Overview</h2>
 * {@code ConsoleSource} reads user input directly from the standard input stream.
 *
 * <p>
 * It wraps {@link System#in} in a {@link BufferedReader} for line-based reading.
 *
 * <h2>Resource handling</h2>
 * The underlying console input stream is <b>not closed</b> when {@link #close()} is called,
 * as closing {@code System.in} may negatively impact the running JVM.
 *
 * <p>
 * This behavior is intentional and safe for interactive CLI applications.
 *
 * @see InputSource
 */
public final class ConsoleSource implements InputSource {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public BufferedReader reader() {
        return reader;
    }

    @Override
    public void close() {
    }
}