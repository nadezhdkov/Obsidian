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

package obsidian.util.stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

public final class Range {
    private Range() {}

    @Contract("_, _, _ -> new")
    public static @NotNull Sequence<Integer> intRange(int start, int end, int step) {
        if (step == 0) throw new IllegalArgumentException("step cannot be 0");

        boolean asc = start <= end;
        int realStep = asc ? Math.abs(step) : -Math.abs(step);

        return new Sequence<>(() -> {
            IntStream s = IntStream.iterate(start, i -> i + realStep);
            return s.takeWhile(i -> asc ? i <= end : i >= end).boxed();
        }, false, null, null);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Sequence<Long> longRange(long start, long end, long step) {
        if (step == 0L) throw new IllegalArgumentException("step cannot be 0");

        boolean asc = start <= end;
        long realStep = asc ? Math.abs(step) : -Math.abs(step);

        return new Sequence<>(() -> {
            LongStream s = LongStream.iterate(start, i -> i + realStep);
            return s.takeWhile(i -> asc ? i <= end : i >= end).boxed();
        }, false, null, null);
    }
}