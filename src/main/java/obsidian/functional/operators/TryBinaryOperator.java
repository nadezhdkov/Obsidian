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

package obsidian.functional.operators;

import obsidian.functional.Try;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface TryBinaryOperator<T> extends BinaryOperator<Try<T>> {

    @Contract(pure = true)
    static <T> @NotNull TryBinaryOperator<T> of(BinaryOperator<T> op) {
        Objects.requireNonNull(op, "op");
        return (a, b) -> a.flatMap(x -> b.map(y -> op.apply(x, y)));
    }

    static <T> @NotNull TryBinaryOperator<T> minBy(Comparator<? super T> comparator) {
        return of(BinaryOperator.minBy(comparator));
    }

    static <T> @NotNull TryBinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        return of(BinaryOperator.maxBy(comparator));
    }

}
