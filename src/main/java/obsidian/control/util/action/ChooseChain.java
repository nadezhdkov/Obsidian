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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class ChooseChain<T> {

    private static final class Step<T> {
        final boolean condition;
        final Supplier<? extends T> supplier;

        Step(boolean condition, Supplier<? extends T> supplier) {
            this.condition = condition;
            this.supplier = supplier;
        }
    }

    private final List<Step<T>> steps = new ArrayList<>();

    private ChooseChain() {}

    public static <T> @NotNull ChooseChain<T> start() {
        return new ChooseChain<>();
    }

    ChooseChain<T> seed(boolean condition, Supplier<? extends T> supplier) {
        steps.add(new Step<>(condition, supplier));
        return this;
    }

    public @NotNull ChooseChain<T> when(boolean condition) {
        steps.add(new Step<>(condition, null));
        return this;
    }

    public @NotNull ChooseChain<T> when(@NotNull BooleanSupplier predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return when(predicate.getAsBoolean());
    }

    public @NotNull ChooseChain<T> thenGet(@NotNull Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (steps.isEmpty()) throw new IllegalStateException("Call when(...) before thenGet(...)");

        Step<T> last = steps.remove(steps.size() - 1);
        if (last.supplier != null) throw new IllegalStateException("thenGet(...) already set for last when(...)");

        steps.add(new Step<>(last.condition, supplier));
        return this;
    }

    public @NotNull ChooseChain<T> then(T value) {
        return thenGet(() -> value);
    }

    public @NotNull ChooseChain<T> elseWhen(boolean condition) {
        return when(condition);
    }

    public @NotNull ChooseChain<T> elseWhen(@NotNull BooleanSupplier predicate) {
        return when(predicate);
    }

    public T otherwise(@NotNull Supplier<? extends T> defaultSupplier) {
        Objects.requireNonNull(defaultSupplier, "defaultSupplier");
        for (Step<T> s : steps) {
            if (s.supplier == null) throw new IllegalStateException("Missing thenGet(...) for a when(...)");
            if (s.condition) return s.supplier.get();
        }
        return defaultSupplier.get();
    }

    public T otherwise(T defaultValue) {
        return otherwise(() -> defaultValue);
    }
}
