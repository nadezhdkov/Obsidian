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

/**
 * Strategy interface responsible for formatting input prompts.
 *
 * <h2>Overview</h2>
 * {@code Prompt} defines how a textual label is transformed into a visible prompt
 * shown to the user.
 *
 * <p>
 * This abstraction allows prompt styling to be customized independently from
 * input logic.
 *
 * <h2>Examples</h2>
 *
 * <h3>Simple prompt</h3>
 * <pre>{@code
 * Prompt p = label -> label + ": ";
 * }</pre>
 *
 * <h3>Decorated prompt</h3>
 * <pre>{@code
 * Prompt p = label -> "[?] " + label + " >> ";
 * }</pre>
 *
 * <h3>Suppressing empty labels</h3>
 * <pre>{@code
 * Prompt p = label -> label == null || label.isBlank()
 *     ? ""
 *     : label + "> ";
 * }</pre>
 *
 * @see Prompts
 * @see obsidian.experimental.io.scan.PromptEnvironment
 */
@FunctionalInterface
public interface Prompt {

    /**
     * Formats the given label into a visible prompt string.
     *
     * @param label the logical prompt label (may be empty or null).
     * @return the formatted prompt string.
     */
    String format(String label);
}
