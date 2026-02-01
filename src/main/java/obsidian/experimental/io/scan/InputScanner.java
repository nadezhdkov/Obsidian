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

package obsidian.experimental.io.scan;

import obsidian.experimental.io.scan.source.ConsoleSource;
import obsidian.experimental.io.scan.source.ReaderSource;
import obsidian.experimental.io.scan.source.StringSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

/**
 * Factory utility for creating {@link InputHandler} instances from different input sources.
 *
 * <h2>Overview</h2>
 * {@code InputScanner} acts as the main entry point for constructing interactive
 * or scripted input scanners.
 *
 * <p>
 * It provides factory methods that wire together:
 * <ul>
 *   <li>an {@link obsidian.experimental.io.scan.source.InputSource}</li>
 *   <li>a {@link PromptEnvironment}</li>
 *   <li>a {@link ConfigurableLineScanner}</li>
 * </ul>
 *
 * allowing users to easily choose where input comes from without manually assembling
 * the underlying components.
 *
 * <h2>Supported input sources</h2>
 * <ul>
 *   <li><b>Console</b> — standard input/output (interactive CLI)</li>
 *   <li><b>String</b> — predefined content (testing, scripting)</li>
 *   <li><b>Reader</b> — custom input sources</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>Console input</h3>
 * <pre>{@code
 * ScannerEngine scan = InputScanner.console();
 * String name = scan.line("Name: ");
 * }</pre>
 *
 * <h3>Console with custom prompt environment</h3>
 * <pre>{@code
 * PromptEnvironment env = PromptEnvironment.defaults()
 *     .withPrefix("> ");
 *
 * ScannerEngine scan = InputScanner.console(env);
 * }</pre>
 *
 * <h3>Scripted input (testing)</h3>
 * <pre>{@code
 * ScannerEngine scan = InputScanner.fromString("10\n20\n30\n");
 * int a = scan.read(Parsers.i32());
 * int b = scan.read(Parsers.i32());
 * }</pre>
 *
 * <h3>Custom reader source</h3>
 * <pre>{@code
 * ScannerEngine scan = InputScanner.fromReader(new FileReader("input.txt"));
 * }</pre>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>This class contains no state.</li>
 *   <li>All methods return new independent {@link InputHandler} instances.</li>
 *   <li>Prompt configuration is optional and defaults are provided.</li>
 * </ul>
 *
 * @see InputHandler
 * @see ConfigurableLineScanner
 * @see PromptEnvironment
 * @see obsidian.experimental.io.scan.source.InputSource
 */
public final class InputScanner {

    private InputScanner() {}

    /**
     * Creates a console-based {@link InputHandler} using the default prompt environment.
     *
     * <p>
     * Input is read from standard input and prompts are printed to standard output.
     */
    @Contract(" -> new")
    public static @NotNull InputHandler console() {
        return new ConfigurableLineScanner(new ConsoleSource(), PromptEnvironment.defaults());
    }

    /**
     * Creates a console-based {@link InputHandler} using a custom {@link PromptEnvironment}.
     *
     * @param config the prompt configuration to use.
     */
    @Contract("_ -> new")
    public static @NotNull InputHandler console(PromptEnvironment config) {
        return new ConfigurableLineScanner(new ConsoleSource(), config);
    }

    /**
     * Creates a scanner that reads input from a predefined string.
     *
     * <p>
     * Useful for testing, scripted execution, or non-interactive environments.
     */
    @Contract("_ -> new")
    public static @NotNull InputHandler fromString(String content) {
        return new ConfigurableLineScanner(new StringSource(content), PromptEnvironment.defaults());
    }

    /**
     * Creates a scanner that reads input from the given {@link Reader}.
     *
     * <p>
     * The caller is responsible for managing the lifecycle of the provided reader.
     */
    @Contract("_ -> new")
    public static @NotNull InputHandler fromReader(Reader reader) {
        return new ConfigurableLineScanner(new ReaderSource(reader), PromptEnvironment.defaults());
    }
}