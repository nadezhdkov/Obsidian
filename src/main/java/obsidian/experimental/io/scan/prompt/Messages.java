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
 * Container for user-facing validation and input messages.
 *
 * <h2>Overview</h2>
 * {@code Messages} defines the textual feedback presented to the user during
 * interactive input scanning.
 *
 * <p>
 * These messages are typically displayed when:
 * <ul>
 *   <li>input parsing fails</li>
 *   <li>value validation fails</li>
 * </ul>
 *
 * <p>
 * The intent of this class is to centralize all user-visible messages, allowing:
 * <ul>
 *   <li>easy customization</li>
 *   <li>localization support</li>
 *   <li>consistent messaging across scanners</li>
 * </ul>
 *
 * <h2>Default messages</h2>
 * The default configuration provides generic, user-friendly feedback:
 *
 * <pre>{@code
 * Invalid entry. Please try again.
 * Value does not meet the criteria. Please try again.
 * }</pre>
 *
 * <h2>Customization example</h2>
 * <pre>{@code
 * Messages custom = new Messages(
 *     "Invalid input!",
 *     "This value is not allowed."
 * );
 * }</pre>
 *
 * @param invalidEntry message displayed when parsing fails
 * @param invalidValue message displayed when validation fails
 *
 * @see obsidian.experimental.io.scan.PromptEnvironment
 */
public record Messages(
        String invalidEntry,
        String invalidValue
) {

    /**
     * Returns the default set of user-facing messages.
     *
     * @return default {@code Messages} instance.
     */
    @Contract(" -> new")
    public static @NotNull Messages defaults() {
        return new Messages(
                "Invalid entry. Please try again.",
                "Value does not meet the criteria. Please try again."
        );
    }
}