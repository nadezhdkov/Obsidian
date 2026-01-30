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
import java.util.function.Supplier;

public final class ActionThen {

    private final boolean  condition;
    private final Runnable thenAction;

    ActionThen(boolean condition, Runnable thenAction) {
        this.condition = condition;
        this.thenAction = thenAction;
    }

    public void run() {
        if (condition) thenAction.run();
    }

    public void otherwise(@NotNull Runnable elseAction) {
        Objects.requireNonNull(elseAction, "elseAction");
        if (condition) thenAction.run();
        else elseAction.run();
    }

    public void orThrow(@NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier");
        if (condition) thenAction.run();
        else throw exceptionSupplier.get();
    }

    public @NotNull DecisionChain elseWhen(boolean nextCondition) {
        return DecisionChain.of()
                .handled(condition)
                .elseWhen(nextCondition)
                .seedThen(thenAction, condition);
    }
}