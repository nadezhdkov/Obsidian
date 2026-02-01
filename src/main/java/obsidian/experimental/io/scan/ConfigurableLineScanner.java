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

import obsidian.experimental.io.scan.validate.ValidationException;
import obsidian.experimental.io.scan.validate.Validator;
import obsidian.experimental.io.scan.source.InputSource;
import obsidian.experimental.io.scan.prompt.Messages;
import obsidian.experimental.io.scan.parse.Parser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Default {@link InputHandler} implementation backed by an {@link InputSource}
 * and a configurable {@link PromptEnvironment}.
 *
 * <h2>Overview</h2>
 * {@code ConfigurableLineScanner} is a line-oriented scanner that:
 * <ul>
 *   <li>reads raw lines from an {@link InputSource}</li>
 *   <li>prints prompts using the configured output streams</li>
 *   <li>parses raw input using a {@link Parser}</li>
 *   <li>validates parsed values using a {@link Validator}</li>
 *   <li>repeats input until parsing + validation succeed</li>
 * </ul>
 *
 * <h2>Prompt environment</h2>
 * The {@link PromptEnvironment} controls how the user interaction behaves, including:
 * <ul>
 *   <li>where prompts are printed ({@code out})</li>
 *   <li>where error messages are printed ({@code err})</li>
 *   <li>prompt formatting ({@link PromptEnvironment#prompt()})</li>
 *   <li>localized/user-facing messages ({@link PromptEnvironment#messages()})</li>
 * </ul>
 *
 * If {@code config} is {@code null}, {@link PromptEnvironment#defaults()} is used.
 *
 * <h2>Error handling model</h2>
 * This implementation follows a simple interactive model:
 * <ul>
 *   <li>If parsing fails (throws {@link RuntimeException}), prints {@link Messages#invalidEntry()}
 *       and re-prompts.</li>
 *   <li>If validation fails (throws {@link ValidationException}), prints {@link Messages#invalidValue()}
 *       and re-prompts.</li>
 * </ul>
 *
 * <h2>I/O behavior</h2>
 * <ul>
 *   <li>{@link #hasNextLine()} uses a {@code mark/reset} peek and returns {@code false} on {@link IOException}.</li>
 *   <li>{@link #line(String)} returns an empty string if an I/O error occurs or EOF is reached.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * {@link #close()} delegates to {@link InputSource#close()} to release underlying resources.
 *
 * @see InputHandler
 * @see InputSource
 * @see PromptEnvironment
 * @see Parser
 * @see Validator
 * @see Messages
 */
final class ConfigurableLineScanner implements InputHandler {

    private final InputSource source;
    private final PromptEnvironment config;

    ConfigurableLineScanner(InputSource source, PromptEnvironment config) {
        this.source = source;
        this.config = config == null ? PromptEnvironment.defaults() : config;
    }

    /**
     * Checks if more input is available by peeking one character from the underlying reader.
     * <p>
     * Uses {@code mark/reset}. Returns {@code false} if an {@link IOException} occurs.
     */
    @Override
    public boolean hasNextLine() {
        try {
            source.reader().mark(1);
            int c = source.reader().read();
            source.reader().reset();
            return c != -1;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Prints a formatted prompt and reads a single raw line from the input source.
     * <p>
     * Returns an empty string if EOF is reached or an {@link IOException} occurs.
     */
    @Override
    public @NotNull String line() {
        return line("");
    }

    /**
     * Reads and parses a value using the given parser.
     * <p>
     * This method performs parsing only and does not apply validation.
     * Internally delegates to {@link #until(String, Parser, Validator)} with a no-op validator.
     */
    @Override
    public @NotNull String line(String prompt) {
        printPrompt(prompt);
        try {
            String ln = source.reader().readLine();
            return ln == null ? "" : ln;
        } catch (IOException e) {
            return "";
        }
    }


    @Override
    public <T> T read(Parser<T> parser) {
        return read("", parser);
    }

    @Override
    public <T> T read(String prompt, Parser<T> parser) {
        return until(prompt, parser, v -> {});
    }

    /**
     * Interactive input loop that keeps prompting until a valid value is produced.
     *
     * <p>
     * Behavior:
     * <ul>
     *   <li>Reads a raw line via {@link #line(String)}</li>
     *   <li>Attempts to parse it with {@link Parser#parse(String)}</li>
     *   <li>If parsing fails, prints {@link Messages#invalidEntry()} and retries</li>
     *   <li>Validates with {@link Validator#validate(Object)}</li>
     *   <li>If validation fails, prints {@link Messages#invalidValue()} and retries</li>
     * </ul>
     *
     * @throws NullPointerException if {@code parser} or {@code validator} is null.
     */
    @Override
    public <T> T until(String prompt, Parser<T> parser, Validator<T> validator) {
        Messages msg = config.messages();

        while (true) {
            String raw = line(prompt);

            final T value;
            try {
                value = parser.parse(raw);
            } catch (RuntimeException ex) {
                config.err().println(msg.invalidEntry());
                continue;
            }

            try {
                validator.validate(value);
                return value;
            } catch (ValidationException vex) {
                config.err().println(msg.invalidValue());
            }
        }
    }

    private void printPrompt(String label) {
        String p = config.prompt().format(label);
        if (!p.isEmpty()) config.out().print(p);
    }

    /**
     * Closes the underlying {@link InputSource}.
     */
    @Override
    public void close() {
        source.close();
    }
}