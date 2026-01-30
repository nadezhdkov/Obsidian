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

import obsidian.functional.Try;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.*;

public final class Sequence<T> {

    private final Supplier<Stream<T>> streamSupplier;

    private final boolean              parallel;
    private final Consumer<Throwable>  errorHandler;
    private final Predicate<? super T> loopUntil;

    Sequence(Supplier<Stream<T>> streamSupplier,
             boolean              parallel,
             Consumer<Throwable>  errorHandler,
             Predicate<? super T> loopUntil) {
        this.streamSupplier = Objects.requireNonNull(streamSupplier, "streamSupplier");
        this.parallel       = parallel;
        this.errorHandler   = errorHandler;
        this.loopUntil      = loopUntil;
    }

    @Contract("_ -> new")
    public static <T> @NotNull Sequence<T> from(Iterable<T> src) {
        Objects.requireNonNull(src, "src");
        return new Sequence<>(() -> StreamSupport.stream(src.spliterator(), false),
                false, null, null);
    }

    @SafeVarargs
    public static <T> @NotNull Sequence<T> of(T... values) {
        Objects.requireNonNull(values, "values");
        return from(Arrays.asList(values));
    }

    @Contract("_, _ -> new")
    public static @NotNull Sequence<Integer> range(int start, int end) {
        return Range.intRange(start, end, 1);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Sequence<Integer> range(int start, int end, int step) {
        return Range.intRange(start, end, step);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Sequence<Long> range(long start, long end, long step) {
        return Range.longRange(start, end, step);
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Sequence<T> parallel() {
        return new Sequence<>(streamSupplier, true, errorHandler, loopUntil);
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Sequence<T> sequential() {
        return new Sequence<>(streamSupplier, false, errorHandler, loopUntil);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Sequence<T> onError(Consumer<Throwable> handler) {
        return new Sequence<>(streamSupplier, parallel, handler, loopUntil);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Sequence<T> loopUntil(Predicate<? super T> stopWhenSeen) {
        return new Sequence<>(streamSupplier, parallel, errorHandler, stopWhenSeen);
    }

    public @NotNull Sequence<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return mapStream(s -> s.filter(predicate));
    }

    @Contract("_ -> new")
    public <R> @NotNull Sequence<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new Sequence<>(() -> build().map(mapper), parallel, errorHandler, null);
    }

    @Contract("_ -> new")
    public <R> @NotNull Sequence<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new Sequence<>(() -> build().flatMap(mapper), parallel, errorHandler, null);
    }

    public @NotNull Sequence<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action");
        return mapStream(s -> s.peek(action));
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Sequence<T> limit(long maxSize) {
        return mapStream(s -> s.limit(maxSize));
    }

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Sequence<T> skip(long n) {
        return mapStream(s -> s.skip(n));
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Sequence<T> reverse() {
        return new Sequence<>(() -> {
            List<T> list = build().collect(Collectors.toList());
            Collections.reverse(list);
            return list.stream();
        }, parallel, errorHandler, null);
    }

    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action");

        Try.run(() -> {
            if (loopUntil == null) {
                build().forEach(action);
            } else {
                loop(build(), action, loopUntil);
            }
        }).onFailure(this::handleError);
    }

    @Contract("_ -> new")
    public @NotNull CompletableFuture<Void> forEachAsync(Consumer<? super T> action) {
        return CompletableFuture.runAsync(() -> forEach(action));
    }

    public Optional<T> first() {
        return Try.of(() -> build().findFirst())
                .peekFailure(this::handleError)
                .getOrElse(Optional.empty());
    }

    public Optional<T> last() {
        return Try.of(() -> {
                    List<T> list = build().toList();
                    return list.isEmpty() ? Optional.<T>empty()
                            : Optional.of(list.getLast());
                }).onFailure(this::handleError)
                .orElse(Try.success(Optional.empty()))
                .get();
    }

    public List<T> toList() {
        return Try.of(() -> build().collect(Collectors.toList()))
                .onFailure(this::handleError)
                .getOrElse(Collections.emptyList());
    }

    public long count() {
        return Try.of(() -> build().count())
                .onFailure(this::handleError)
                .getOrElse(0L);
    }

    public Stream<T> stream() {
        return Try.of(this::build)
                .onFailure(this::handleError)
                .getOrElse(Stream.empty());
    }

    @Contract(value = "_ -> new", pure = true)
    private @NotNull Sequence<T> mapStream(UnaryOperator<Stream<T>> op) {
        return new Sequence<>(() -> op.apply(build()), parallel, errorHandler, loopUntil);
    }

    private Stream<T> build() {
        Stream<T> s = streamSupplier.get();
        return parallel ? s.parallel() : s.sequential();
    }

    private void loop(@NotNull Stream<T> stream, Consumer<? super T> action, Predicate<? super T> stopWhenSeen) {
        List<T> list = stream.toList();
        if (list.isEmpty()) return;

        boolean stop = false;
        while (!stop) {
            for (T element : list) {
                action.accept(element);
                if (stopWhenSeen.test(element)) {
                    stop = true;
                    break;
                }
            }
        }
    }

    private void handleError(Throwable t) {
        if (errorHandler != null) errorHandler.accept(t);
        else {
            System.err.println("[Seq] Error in pipeline:");
            t.printStackTrace();
        }
    }
}