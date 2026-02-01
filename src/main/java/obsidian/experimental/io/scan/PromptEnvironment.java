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

import obsidian.experimental.io.scan.prompt.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Encapsulates all configuration related to user interaction and prompt behavior.
 *
 * <h2>Overview</h2>
 * {@code PromptEnvironment} represents the complete environment used during
 * interactive input scanning.
 *
 * <p>
 * It centralizes all user-facing configuration in a single immutable structure,
 * including:
 * <ul>
 *   <li>locale information</li>
 *   <li>output and error streams</li>
 *   <li>prompt formatting rules</li>
 *   <li>user-visible messages</li>
 * </ul>
 *
 * <p>
 * This object is consumed by {@link ConfigurableLineScanner} to determine how
 * prompts are rendered and how feedback is presented to the user.
 *
 * <h2>Why a record?</h2>
 * {@code PromptEnvironment} is implemented as a {@code record} because:
 * <ul>
 *   <li>it represents pure configuration</li>
 *   <li>it is immutable</li>
 *   <li>it has value-based semantics</li>
 *   <li>it is safe to share across scanners</li>
 * </ul>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link #locale()} — locale used for message localization</li>
 *   <li>{@link #out()} — output stream used for prompts</li>
 *   <li>{@link #err()} — output stream used for validation and error messages</li>
 *   <li>{@link #prompt()} — prompt formatting strategy</li>
 *   <li>{@link #messages()} — localized user-facing messages</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>Default environment</h3>
 * <pre>{@code
 * PromptEnvironment env = PromptEnvironment.defaults();
 * ScannerEngine scan = InputScanner.console(env);
 * }</pre>
 *
 * <h3>Custom output streams</h3>
 * <pre>{@code
 * PromptEnvironment env = new PromptEnvironment(
 *     Locale.US,
 *     System.out,
 *     System.err,
 *     Prompts.defaultPrompt(),
 *     Messages.defaults()
 * );
 * }</pre>
 *
 * <h3>Custom prompt style</h3>
 * <pre>{@code
 * PromptEnvironment env = PromptEnvironment.defaults()
 *     .withPrompt(label -> "[?] " + label);
 * }</pre>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>This class contains no logic — only configuration.</li>
 *   <li>Behavior is implemented by {@link InputHandler} implementations.</li>
 *   <li>All fields are expected to be non-null.</li>
 * </ul>
 *
 * @param locale   locale used for message localization
 * @param out      output stream used for prompts
 * @param err      output stream used for error messages
 * @param prompt   strategy used to format prompt labels
 * @param messages localized user-facing messages
 *
 * @see Prompt
 * @see Prompts
 * @see Messages
 * @see InputHandler
 */
public record PromptEnvironment(
        Locale locale,
        PrintStream out,
        PrintStream err,
        Prompt prompt,
        Messages messages
) {

    /**
     * Returns the default prompt environment.
     *
     * <p>
     * The default configuration uses:
     * <ul>
     *   <li>{@link Locale#getDefault()}</li>
     *   <li>{@link System#out} for standard output</li>
     *   <li>{@link System#err} for error output</li>
     *   <li>{@link Prompts#defaultPrompt()}</li>
     *   <li>{@link Messages#defaults()}</li>
     * </ul>
     *
     * @return a default {@code PromptEnvironment} instance.
     */
    @Contract(" -> new")
    public static @NotNull PromptEnvironment defaults() {
        return new PromptEnvironment(
                Locale.getDefault(),
                System.out,
                System.err,
                Prompts.defaultPrompt(),
                Messages.defaults()
        );
    }
}
