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

package obsidian.control.util.action;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.*;

public final class ActionWhen {

    private final boolean condition;

    private ActionWhen(boolean condition) {
        this.condition = condition;
    }

    public static @NotNull ActionWhen of(boolean condition) {
        return new ActionWhen(condition);
    }

    public @NotNull ActionWhen and(boolean other) {
        return new ActionWhen(this.condition && other);
    }

    public @NotNull ActionWhen and(@NotNull BooleanSupplier other) {
        Objects.requireNonNull(other, "other");
        return new ActionWhen(this.condition && other.getAsBoolean());
    }

    public @NotNull ActionWhen or(boolean other) {
        return new ActionWhen(this.condition || other);
    }

    public @NotNull ActionWhen or(@NotNull BooleanSupplier other) {
        Objects.requireNonNull(other, "other");
        return new ActionWhen(this.condition || other.getAsBoolean());
    }

    public @NotNull ActionWhen negate() {
        return new ActionWhen(!this.condition);
    }

    public @NotNull ActionThen then(@NotNull Runnable action) {
        Objects.requireNonNull(action, "action");
        return new ActionThen(this.condition, action);
    }

    public <T> @NotNull ActionThen thenAccept(T value, @NotNull Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return then(() -> consumer.accept(value));
    }

    public void thenThrow(@NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier");
        if (condition) throw exceptionSupplier.get();
    }
}