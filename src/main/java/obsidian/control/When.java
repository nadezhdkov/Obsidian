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

package obsidian.control;

import obsidian.control.util.action.ActionWhen;
import obsidian.control.util.action.ChooseChain;
import obsidian.control.util.action.ChooseWhen;
import obsidian.control.util.action.DecisionChain;
import obsidian.control.util.match.MatchPattern;
import obsidian.control.util.require.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

@SuppressWarnings("unused")
public final class When {

    private When() {}

    public static @NotNull ActionWhen when(boolean condition) {
        return ActionWhen.of(condition);
    }

    public static @NotNull ActionWhen when(@NotNull BooleanSupplier predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return ActionWhen.of(predicate.getAsBoolean());
    }

    public static @NotNull ActionWhen whenNotNull(@Nullable Object object) {
        return ActionWhen.of(object != null);
    }

    public static @NotNull ActionWhen whenNull(@Nullable Object object) {
        return ActionWhen.of(object == null);
    }

    public static @NotNull ActionWhen whenPresent(@NotNull Optional<?> optional) {
        Objects.requireNonNull(optional, "optional");
        return ActionWhen.of(optional.isPresent());
    }

    public static @NotNull ActionWhen whenEmpty(@NotNull Optional<?> optional) {
        Objects.requireNonNull(optional, "optional");
        return ActionWhen.of(optional.isEmpty());
    }

    public static @NotNull DecisionChain chain() {
        return DecisionChain.of();
    }

    public static <T> @NotNull ChooseWhen<T> value(boolean condition) {
        return ChooseWhen.of(condition);
    }

    public static <T> @NotNull ChooseChain<T> choose() {
        return ChooseChain.start();
    }

    public static <T> @NotNull MatchPattern<T> match(@Nullable T value) {
        return MatchPattern.of(value);
    }

    public static void throwIf(boolean condition, @NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
        Preconditions.throwIf(condition, exceptionSupplier);
    }

    public static void throwUnless(boolean condition, @NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
        Preconditions.throwUnless(condition, exceptionSupplier);
    }

    public static void requireTrue(boolean condition, @NotNull String message) {
        Preconditions.requireTrue(condition, message);
    }

    public static void requireFalse(boolean condition, @NotNull String message) {
        Preconditions.requireFalse(condition, message);
    }

    public static <T> @NotNull T requireNonNull(@Nullable T object, @NotNull String message) {
        return Preconditions.requireNonNull(object, message);
    }

    public static void ifElse(boolean condition, @NotNull Runnable ifTrue, @NotNull Runnable ifFalse) {
        Objects.requireNonNull(ifTrue, "ifTrue");
        Objects.requireNonNull(ifFalse, "ifFalse");
        if (condition) ifTrue.run();
        else ifFalse.run();
    }

    public static <T> T choose(boolean condition, @NotNull Supplier<T> ifTrue, @NotNull Supplier<T> ifFalse) {
        Objects.requireNonNull(ifTrue, "ifTrue");
        Objects.requireNonNull(ifFalse, "ifFalse");
        return condition ? ifTrue.get() : ifFalse.get();
    }

    public static <T> T choose(boolean condition, T ifTrue, T ifFalse) {
        return condition ? ifTrue : ifFalse;
    }

    public static void onlyIf(boolean condition, @NotNull Runnable action) {
        Objects.requireNonNull(action, "action");
        if (condition) action.run();
    }

    public static void unless(boolean condition, @NotNull Runnable action) {
        Objects.requireNonNull(action, "action");
        if (!condition) action.run();
    }
}