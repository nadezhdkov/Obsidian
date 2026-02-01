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

import obsidian.experimental.io.scan.parse.Parser;
import obsidian.experimental.io.scan.validate.Validator;

/**
 * Core abstraction for interactive input engines.
 *
 * <h2>Overview</h2>
 * {@code InputHandler} defines the contract for components capable of reading
 * user input in an interactive manner.
 *
 * <p>
 * It acts as the execution backend for the experimental {@link Scan} facade and is
 * responsible for:
 * <ul>
 *   <li>displaying prompts</li>
 *   <li>reading raw input</li>
 *   <li>delegating parsing to {@link Parser}</li>
 *   <li>delegating validation to {@link Validator}</li>
 *   <li>repeating input until a valid value is produced</li>
 * </ul>
 *
 * <p>
 * Implementations may source input from:
 * <ul>
 *   <li>standard console</li>
 *   <li>predefined/mock input</li>
 *   <li>files</li>
 *   <li>remote or scripted sources</li>
 * </ul>
 *
 * <h2>Input model</h2>
 * The typical flow implemented by a {@code InputHandler} is:
 *
 * <ol>
 *   <li>Display prompt (if provided)</li>
 *   <li>Read raw line</li>
 *   <li>Parse input using {@link Parser}</li>
 *   <li>Validate parsed value using {@link Validator}</li>
 *   <li>Repeat until success</li>
 * </ol>
 *
 * This design cleanly separates concerns between:
 * <ul>
 *   <li>I/O handling (engine)</li>
 *   <li>parsing logic ({@link Parser})</li>
 *   <li>validation rules ({@link Validator})</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <pre>{@code
 * InputHandler engine = InputScanner.console();
 *
 * int age = engine.read("Age: ", Parsers.i32());
 *
 * String name = engine.until(
 *     "Name: ",
 *     Parsers.string(),
 *     Validators.notBlank()
 * );
 * }</pre>
 *
 * <h2>Lifecycle</h2>
 * Implementations may hold system resources such as input streams.
 * For this reason, {@code InputHandler} extends {@link AutoCloseable}.
 *
 * <p>
 * When no longer needed, {@link #close()} should be called to release resources.
 *
 * <h2>Thread-safety</h2>
 * Implementations are not required to be thread-safe unless explicitly documented.
 *
 * @see Scan
 * @see Parser
 * @see Validator
 */
public interface InputHandler extends AutoCloseable {

    /**
     * Indicates whether another input line is available.
     *
     * @return {@code true} if input can still be read.
     */
    boolean hasNextLine();

    /**
     * Reads a raw input line without displaying a prompt.
     *
     * @return the input line.
     */
    String line();

    /**
     * Displays the given prompt and reads a raw input line.
     *
     * @param prompt the prompt message to display.
     * @return the input line.
     */
    String line(String prompt);

    /**
     * Reads input and parses it using the given {@link Parser}.
     *
     * @param parser the parser responsible for converting raw input.
     * @param <T> the parsed type.
     * @return the parsed value.
     */
    <T> T read(Parser<T> parser);

    /**
     * Displays a prompt, reads input and parses it using the given {@link Parser}.
     *
     * @param prompt the prompt to display.
     * @param parser the parser used to convert input.
     * @param <T> the parsed type.
     * @return the parsed value.
     */
    <T> T read(String prompt, Parser<T> parser);

    /**
     * Repeatedly reads input until both parsing and validation succeed.
     *
     * <p>
     * This method represents the full interactive input loop:
     * <ul>
     *   <li>prompt</li>
     *   <li>read</li>
     *   <li>parse</li>
     *   <li>validate</li>
     * </ul>
     *
     * @param prompt the prompt to display.
     * @param parser the parser used to convert input.
     * @param validator the validator applied to the parsed value.
     * @param <T> the resulting type.
     * @return a validated value.
     */
    <T> T until(String prompt, Parser<T> parser, Validator<T> validator);

    /**
     * Closes the engine and releases any underlying resources.
     */
    @Override
    void close();

}