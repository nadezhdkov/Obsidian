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

package obsidian.experimental.io.scan.prompt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Factory utilities for common {@link Prompt} implementations.
 *
 * <h2>Overview</h2>
 * {@code Prompts} provides reusable prompt formatting strategies used by
 * the input scanning system.
 *
 * <p>
 * Prompts are intentionally lightweight and stateless.
 *
 * <h2>Default prompt</h2>
 * The default prompt formats labels as:
 *
 * <pre>{@code
 * label>
 * }</pre>
 *
 * If the label is {@code null} or blank, an empty string is returned
 * and no prompt is displayed.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Prompt prompt = Prompts.defaultPrompt();
 * System.out.print(prompt.format("Age"));
 * }</pre>
 *
 * @see Prompt
 * @see obsidian.experimental.io.scan.PromptEnvironment
 */
public final class Prompts {

    private Prompts() {
    }

    /**
     * Returns the default prompt formatter.
     *
     * <p>
     * Behavior:
     * <ul>
     *   <li>If label is {@code null} or blank → returns empty string</li>
     *   <li>Otherwise → returns {@code label + "> "}</li>
     * </ul>
     *
     * @return default {@link Prompt} implementation.
     */
    @Contract(pure = true)
    public static @NotNull Prompt defaultPrompt() {
        return label -> (label == null || label.isBlank()) ? "" : (label + "> ");
    }
}