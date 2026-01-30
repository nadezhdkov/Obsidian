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

package obsidian.control.util.require;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class Preconditions {

    private Preconditions() {}

    public static void throwIf(boolean condition, @NotNull Supplier<? extends RuntimeException> ex) {
        Objects.requireNonNull(ex, "ex");
        if (condition) throw ex.get();
    }

    public static void throwUnless(boolean condition, @NotNull Supplier<? extends RuntimeException> ex) {
        Objects.requireNonNull(ex, "ex");
        if (!condition) throw ex.get();
    }

    public static void requireTrue(boolean condition, @NotNull String message) {
        Objects.requireNonNull(message, "message");
        if (!condition) throw new IllegalArgumentException(message);
    }

    public static void requireFalse(boolean condition, @NotNull String message) {
        Objects.requireNonNull(message, "message");
        if (condition) throw new IllegalArgumentException(message);
    }

    public static <T> @NotNull T requireNonNull(@Nullable T obj, @NotNull String message) {
        Objects.requireNonNull(message, "message");
        if (obj == null) throw new NullPointerException(message);
        return obj;
    }
}