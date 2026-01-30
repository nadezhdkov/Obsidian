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

package obsidian.control.util.match;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class MatchPattern<T> {

    private final @Nullable T value;

    private Supplier<?> chosen;
    private boolean     matched;

    private MatchPattern(@Nullable T value) {
        this.value = value;
    }

    public static <T> @NotNull MatchPattern<T> of(@Nullable T value) {
        return new MatchPattern<>(value);
    }

    public <R> @NotNull MatchPattern<T> caseOf(@Nullable T pattern, @NotNull Supplier<? extends R> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (!matched && Objects.equals(value, pattern)) {
            chosen  = supplier;
            matched = true;
        }
        return this;
    }

    public <R> @NotNull MatchPattern<T> caseOf(@Nullable T pattern, R result) {
        return caseOf(pattern, () -> result);
    }

    public <R> @NotNull MatchPattern<T> caseWhen(@NotNull Predicate<? super T> predicate,
                                                 @NotNull Supplier<? extends R> supplier) {
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(supplier, "supplier");
        if (!matched && predicate.test(value)) {
            chosen = supplier;
            matched = true;
        }
        return this;
    }

    public <R> @NotNull MatchPattern<T> caseWhen(@NotNull Predicate<? super T> predicate, R result) {
        return caseWhen(predicate, () -> result);
    }

    public <S extends T, R> @NotNull MatchPattern<T> caseType(@NotNull Class<S> type,
                                                              @NotNull Function<? super S, ? extends R> mapper) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(mapper, "mapper");
        if (!matched && type.isInstance(value)) {
            S casted = type.cast(value);
            chosen = (Supplier<R>) () -> mapper.apply(casted);
            matched = true;
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <R> R otherwise(@NotNull Supplier<? extends R> defaultSupplier) {
        Objects.requireNonNull(defaultSupplier, "defaultSupplier");
        if (matched) return ((Supplier<? extends R>) chosen).get();
        return defaultSupplier.get();
    }

    public <R> R otherwise(R defaultValue) {
        return otherwise(() -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <R> @Nullable R orNull() {
        if (!matched) return null;
        return ((Supplier<? extends R>) chosen).get();
    }

    public <R> @NotNull Optional<R> asOptional() {
        return Optional.ofNullable(orNull());
    }

    @SuppressWarnings("unchecked")
    public <R> R orThrow(@NotNull Supplier<? extends RuntimeException> ex) {
        Objects.requireNonNull(ex, "ex");
        if (matched) return ((Supplier<? extends R>) chosen).get();
        throw ex.get();
    }
}