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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ChooseThen<T> {

    private final boolean condition;
    private final Supplier<? extends T> thenSupplier;

    ChooseThen(boolean condition, Supplier<? extends T> thenSupplier) {
        this.condition = condition;
        this.thenSupplier = thenSupplier;
    }

    public T otherwise(@NotNull Supplier<? extends T> elseSupplier) {
        Objects.requireNonNull(elseSupplier, "elseSupplier");
        return condition ? thenSupplier.get() : elseSupplier.get();
    }

    public T otherwise(T elseValue) {
        return condition ? thenSupplier.get() : elseValue;
    }

    public @Nullable T orNull() {
        return condition ? thenSupplier.get() : null;
    }

    public @NotNull Optional<T> asOptional() {
        return condition ? Optional.ofNullable(thenSupplier.get()) : Optional.empty();
    }

    public T orThrow(@NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier");
        if (condition) return thenSupplier.get();
        throw exceptionSupplier.get();
    }

    public @NotNull ChooseChain<T> elseWhen(boolean nextCondition) {
        return ChooseChain.<T>start()
                .seed(condition, thenSupplier)
                .elseWhen(nextCondition);
    }

    public <R> @NotNull ChooseThen<R> map(@NotNull Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new ChooseThen<>(condition, () -> mapper.apply(thenSupplier.get()));
    }
}