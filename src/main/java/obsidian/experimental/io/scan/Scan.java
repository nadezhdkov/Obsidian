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

import obsidian.experimental.io.scan.parse.*;
import obsidian.experimental.io.scan.validate.*;

/**
 * Static facade for interactive input scanning.
 *
 * <h2>Overview</h2>
 * {@code Scan} provides a simple, high-level API for reading and validating user input,
 * acting as a facade over a configurable {@link InputHandler}.
 *
 * <p>
 * This class is designed for interactive environments such as:
 * <ul>
 *   <li>console applications</li>
 *   <li>CLI tools</li>
 *   <li>prototypes and experiments</li>
 * </ul>
 *
 * <p>
 * All input operations are delegated to an underlying {@link InputHandler}, which can be
 * replaced at runtime using {@link #use(InputHandler)}.
 *
 * <h2>Default engine</h2>
 * By default, {@code Scan} uses:
 *
 * <pre>{@code
 * InputScanner.console()
 * }</pre>
 *
 * which reads from standard input and writes prompts to standard output.
 *
 * <h2>Parsers and validators</h2>
 * Input reading is based on two core concepts:
 *
 * <ul>
 *   <li>{@link Parser} — converts raw input into a typed value</li>
 *   <li>{@link Validator} — validates parsed values and decides whether input is accepted</li>
 * </ul>
 *
 * These components can be freely combined to build expressive input flows.
 *
 * <h2>Examples</h2>
 *
 * <h3>Basic input</h3>
 * <pre>{@code
 * String name = Scan.line("Name: ");
 * int age = Scan.i32("Age: ");
 * }</pre>
 *
 * <h3>Typed parsing</h3>
 * <pre>{@code
 * int port = Scan.i32("Port: ");
 * boolean enabled = Scan.bool("Enabled? ");
 * }</pre>
 *
 * <h3>Validated input</h3>
 * <pre>{@code
 * String username = Scan.notBlank("Username: ");
 * }</pre>
 *
 * <h3>Custom parsing + validation</h3>
 * <pre>{@code
 * int level = Scan.until(
 *     "Level: ",
 *     Parsers.i32(),
 *     Validators.range(1, 100)
 * );
 * }</pre>
 *
 * <h2>Replacing the engine</h2>
 * The underlying scanner implementation can be replaced globally:
 *
 * <pre>{@code
 * Scan.use(InputScanner.mock("42"));
 * }</pre>
 *
 * This is especially useful for:
 * <ul>
 *   <li>testing</li>
 *   <li>automated input</li>
 *   <li>custom I/O sources</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * The underlying engine may hold system resources. When finished, invoke:
 *
 * <pre>{@code
 * Scan.close();
 * }</pre>
 *
 * to release them.
 *
 * <h2>Experimental status</h2>
 * This API is marked as experimental and may change without backward compatibility
 * guarantees in future versions.
 *
 * @see InputHandler
 * @see Parser
 * @see Validator
 * @see Parsers
 * @see Validators
 */
public final class Scan {

    private static volatile InputHandler DEFAULT = InputScanner.console();

    private Scan() {
    }

    /**
     * Replaces the default {@link InputHandler} used by this facade.
     *
     * @param engine the new engine to use; ignored if {@code null}.
     */
    public static void use(InputHandler engine) {
        if (engine != null) DEFAULT = engine;
    }

    /**
     * Reads a raw input line.
     */
    public static String line() {
        return DEFAULT.line();
    }

    /**
     * Reads a raw input line after displaying a prompt.
     */
    public static String line(String prompt) {
        return DEFAULT.line(prompt);
    }

    /**
     * Reads a 32-bit integer from input using {@link Parsers#i32()}.
     */
    public static int     i32(String prompt) {
        return DEFAULT.read(prompt, Parsers.i32());
    }

    /**
     * Reads a 64-bit integer from input using {@link Parsers#i64()}.
     */
    public static long    i64(String prompt) {
        return DEFAULT.read(prompt, Parsers.i64());
    }

    /**
     * Reads a 64-bit floating-point value from input using {@link Parsers#f64()}.
     */
    public static double  f64(String prompt) {
        return DEFAULT.read(prompt, Parsers.f64());
    }

    /**
     * Reads a boolean value from input using {@link Parsers#bool()}.
     */
    public static boolean bool(String prompt) {
        return DEFAULT.read(prompt, Parsers.bool());
    }

    /**
     * Reads a non-null string value from input.
     */
    public static String  str(String prompt) {
        return DEFAULT.read(prompt, Parsers.string());
    }

    /**
     * Reads input repeatedly until both parsing and validation succeed.
     *
     * @param prompt the prompt to display
     * @param parser the parser used to convert raw input
     * @param v the validator applied to the parsed value
     */
    public static <T> T until(String prompt, Parser<T> parser, Validator<T> v) {
        return DEFAULT.until(prompt, parser, v);
    }

    /**
     * Reads a non-blank string value.
     */
    public static String notBlank(String prompt) {
        return DEFAULT.until(prompt, Parsers.string(), Validators.notBlank());
    }

    /**
     * Closes the underlying {@link InputHandler} and releases any resources.
     */
    public static void close() {
        DEFAULT.close();
    }
}