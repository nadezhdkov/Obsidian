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

public final class DecisionChain {

    private static final class Step {
        final boolean  condition;
        final Runnable action;

        Step(boolean condition, Runnable action) {
            this.condition = condition;
            this.action    = action;
        }
    }

    private final List<Step> steps   = new ArrayList<>();
    private boolean          handled = false;

    private DecisionChain() {}

    public static @NotNull DecisionChain of() {
        return new DecisionChain();
    }

    DecisionChain handled(boolean handled) {
        this.handled = handled;
        return this;
    }

    DecisionChain seedThen(Runnable thenAction, boolean condition) {
        steps.add(new Step(condition, thenAction));
        return this;
    }

    public @NotNull DecisionChain when(boolean condition) {
        steps.add(new Step(condition, null));
        return this;
    }

    public @NotNull DecisionChain when(@NotNull BooleanSupplier predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return when(predicate.getAsBoolean());
    }

    public @NotNull DecisionChain then(@NotNull Runnable action) {
        Objects.requireNonNull(action, "action");
        if (steps.isEmpty()) throw new IllegalStateException("Call when(...) before then(...)");

        Step last = steps.removeLast();
        if (last.action != null) {
            throw new IllegalStateException("then(...) already set for last when(...)");
        }
        steps.add(new Step(last.condition, action));
        return this;
    }

    public @NotNull DecisionChain elseWhen(boolean condition) {
        return when(condition);
    }

    public @NotNull DecisionChain elseWhen(@NotNull BooleanSupplier predicate) {
        return when(predicate);
    }

    public void otherwise(@NotNull Runnable elseAction) {
        Objects.requireNonNull(elseAction, "elseAction");
        if (tryHandle()) return;
        elseAction.run();
    }

    public void run() {
        tryHandle();
    }

    private boolean tryHandle() {
        if (handled) return true;

        for (Step s : steps) {
            if (s.action == null) throw new IllegalStateException("Missing then(...) for a when(...)");
            if (s.condition) {
                s.action.run();
                handled = true;
                return true;
            }
        }
        return false;
    }
}