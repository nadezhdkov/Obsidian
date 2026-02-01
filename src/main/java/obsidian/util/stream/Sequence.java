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

/**
 * Functional sequence abstraction built on top of {@link Stream}, providing
 * a safer, more expressive and reusable iteration pipeline.
 *
 * <h2>Overview</h2>
 * {@code Sequence} is a high-level utility designed to simplify common stream workflows
 * while adding features not natively supported by Java Streams:
 *
 * <ul>
 *   <li>Lazy stream recreation via {@link Supplier}</li>
 *   <li>Optional parallel or sequential execution</li>
 *   <li>Centralized error handling</li>
 *   <li>Controlled looping with {@link #loopUntil(Predicate)}</li>
 *   <li>Safe terminal operations using {@link Try}</li>
 * </ul>
 *
 * Unlike {@link Stream}, a {@code Sequence} can be consumed multiple times, since the
 * underlying stream is rebuilt on demand.
 *
 * <h2>Key differences from {@link Stream}</h2>
 * <ul>
 *   <li><b>Reusable</b>: streams are recreated for each terminal operation.</li>
 *   <li><b>Error-aware</b>: failures are captured and routed through an error handler.</li>
 *   <li><b>Configurable execution</b>: supports sequential or parallel mode.</li>
 *   <li><b>Extended control flow</b>: supports looping until a condition is met.</li>
 * </ul>
 *
 * <h2>Creation</h2>
 * Sequences can be created from:
 *
 * <pre>{@code
 * Sequence.from(iterable);
 * Sequence.of(1, 2, 3);
 * Sequence.range(0, 10);
 * }</pre>
 *
 * <h2>Pipeline operations</h2>
 * Most intermediate operations mirror {@link Stream} semantics while preserving immutability:
 *
 * <pre>{@code
 * Sequence.range(1, 10)
 *     .filter(n -> n % 2 == 0)
 *     .map(n -> n * 2)
 *     .peek(System.out::println)
 *     .toList();
 * }</pre>
 *
 * <h2>Loop control</h2>
 * {@link #loopUntil(Predicate)} enables repeated iteration over the sequence until a
 * condition is met.
 *
 * <pre>{@code
 * Sequence.of("A", "B", "STOP", "C")
 *     .loopUntil(v -> v.equals("STOP"))
 *     .forEach(System.out::println);
 * }</pre>
 *
 * In this example, the sequence is repeatedly iterated until the element {@code "STOP"}
 * is encountered.
 *
 * <h2>Parallel execution</h2>
 * Execution mode can be configured fluently:
 *
 * <pre>{@code
 * Sequence.range(0, 1_000_000)
 *     .parallel()
 *     .forEach(System.out::println);
 * }</pre>
 *
 * <p>
 * Parallel mode internally delegates to {@link Stream#parallel()}.
 *
 * <h2>Error handling</h2>
 * Errors occurring during pipeline execution are handled through {@link Try} and can be
 * intercepted via {@link #onError(Consumer)}:
 *
 * <pre>{@code
 * Sequence.range(1, 10)
 *     .map(n -> 10 / (n - 5))
 *     .onError(err -> System.err.println("Error: " + err.getMessage()))
 *     .forEach(System.out::println);
 * }</pre>
 *
 * If no error handler is defined, errors are printed to {@code System.err}.
 *
 * <h2>Terminal operations</h2>
 * Supported terminal operations include:
 * <ul>
 *   <li>{@link #forEach(Consumer)}</li>
 *   <li>{@link #forEachAsync(Consumer)}</li>
 *   <li>{@link #first()}</li>
 *   <li>{@link #last()}</li>
 *   <li>{@link #toList()}</li>
 *   <li>{@link #count()}</li>
 *   <li>{@link #stream()}</li>
 * </ul>
 *
 * All terminal operations are safe and never throw unchecked exceptions directly unless
 * explicitly rethrown by user code.
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>All operations return new {@code Sequence} instances (immutable pipeline).</li>
 *   <li>Intermediate operations remain lazy.</li>
 *   <li>Terminal operations rebuild the stream.</li>
 *   <li>{@link #loopUntil(Predicate)} materializes the sequence internally.</li>
 * </ul>
 *
 * @param <T> the element type of the sequence
 *
 * @see Stream
 * @see Try
 */
public final class Sequence<T> {

    /* ========================== Fields ========================== */

    private final Supplier<Stream<T>> streamSupplier;

    private final boolean              parallel;
    private final Consumer<Throwable>  errorHandler;
    private final Predicate<? super T> loopUntil;

    /* ========================== Constructor ========================== */

    Sequence(Supplier<Stream<T>> streamSupplier,
             boolean              parallel,
             Consumer<Throwable>  errorHandler,
             Predicate<? super T> loopUntil
    ) {
        this.streamSupplier = Objects.requireNonNull(streamSupplier, "streamSupplier");
        this.parallel       = parallel;
        this.errorHandler   = errorHandler;
        this.loopUntil      = loopUntil;
    }

    /* ========================== Factory Methods ========================== */

    /**
     * Creates a new {@code Sequence} instance from the provided {@code Iterable} source.
     *
     * @param src the {@code Iterable} source to create the sequence from; must not be {@code null}
     * @return a new {@code Sequence} representing the elements of the provided iterable
     * @throws NullPointerException if {@code src} is {@code null}
     */
    @Contract("_ -> new")
    public static <T> @NotNull Sequence<T> from(Iterable<T> src) {
        Objects.requireNonNull(src, "src");
        return new Sequence<>(() -> StreamSupport.stream(src.spliterator(), false),
                false, null, null);
    }

    /**
     * Creates a new {@code Sequence} instance from the provided array of values.
     *
     * @param <T>    the type of elements in the sequence
     * @param values the array of values to create the sequence from; must not be {@code null}
     * @return a new {@code Sequence} representing the elements of the provided array
     * @throws NullPointerException if {@code values} is {@code null}
     */
    @SafeVarargs
    public static <T> @NotNull Sequence<T> of(T... values) {
        Objects.requireNonNull(values, "values");
        return from(Arrays.asList(values));
    }

    /**
     * Creates a new {@code Sequence} of {@code Integer} values representing a range of integers
     * starting from the {@code start} value (inclusive) to the {@code end} value (inclusive)
     * incremented by a step of 1.
     *
     * @param start the starting value of the range (inclusive)
     * @param end   the ending value of the range (inclusive)
     * @return a {@code Sequence} of {@code Integer} values within the specified range
     */
    @Contract("_, _ -> new")
    public static @NotNull Sequence<Integer> range(int start, int end) {
        return Range.intRange(start, end, 1);
    }

    /**
     * Creates a new {@code Sequence} of {@code Integer} values representing a range of integers
     * starting from the {@code start} value (inclusive) to the {@code end} value (inclusive)
     * incremented by the specified {@code step} value.
     *
     * @param start the starting value of the range (inclusive)
     * @param end   the ending value of the range (inclusive)
     * @param step  the step value for iteration; must not be {@code 0}
     * @return a {@code Sequence} of {@code Integer} values within the specified range
     * @throws IllegalArgumentException if {@code step} is {@code 0}
     */
    @Contract("_, _, _ -> new")
    public static @NotNull Sequence<Integer> range(int start, int end, int step) {
        return Range.intRange(start, end, step);
    }

    /**
     * Creates a new {@code Sequence} of {@code Long} values representing a range of long integers
     * starting from the {@code start} value (inclusive) to the {@code end} value (inclusive)
     * incremented by the specified {@code step} value.
     *
     * @param start the starting value of the range (inclusive)
     * @param end   the ending value of the range (inclusive)
     * @param step  the step value for iteration; must not be {@code 0}
     * @return a {@code Sequence} of {@code Long} values within the specified range
     * @throws IllegalArgumentException if {@code step} is {@code 0}
     */
    @Contract("_, _, _ -> new")
    public static @NotNull Sequence<Long> range(long start, long end, long step) {
        return Range.longRange(start, end, step);
    }

    /* ========================== Execution Mode ========================== */

    /**
     * Returns a new {@code Sequence} instance configured to process elements in parallel.
     * This method enables parallel processing for the sequence, which may provide
     * performance benefits on large datasets or computationally intensive operations,
     * depending on the environment and workload characteristics.
     *
     * @return a new {@code Sequence} with parallel processing enabled
     */
    @Contract(value = " -> new", pure = true)
    public @NotNull Sequence<T> parallel() {
        return new Sequence<>(streamSupplier, true, errorHandler, loopUntil);
    }

    /**
     * Returns a new {@code Sequence} instance configured to process elements sequentially.
     * This method disables parallel processing for the sequence and ensures that
     * all operations are executed in a sequential order.
     *
     * @return a new {@code Sequence} with sequential processing enabled
     */
    @Contract(value = " -> new", pure = true)
    public @NotNull Sequence<T> sequential() {
        return new Sequence<>(streamSupplier, false, errorHandler, loopUntil);
    }

    /* ========================== Error Handling ========================== */

    /**
     * Returns a new {@code Sequence} instance configured to handle errors using the specified error handler.
     * The provided {@code handler} will be invoked whenever an error occurs during the execution of
     * sequence operations.
     *
     * @param handler a {@code Consumer} that accepts a {@code Throwable} and defines the logic
     *                for handling errors; must not be {@code null}.
     * @return a new {@code Sequence} instance with the specified error handler applied.
     * @throws NullPointerException if {@code handler} is {@code null}.
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull Sequence<T> onError(Consumer<Throwable> handler) {
        return new Sequence<>(streamSupplier, parallel, handler, loopUntil);
    }

    /* ========================== Flow Control ========================== */

    /**
     * Returns a new {@code Sequence} instance that processes elements in the current sequence
     * and stops processing once an element satisfies the specified {@code stopWhenSeen} condition.
     * This allows for conditional termination based on the provided predicate.
     *
     * @param stopWhenSeen a {@code Predicate} that determines the stopping condition.
     *                     Processing will halt when this condition evaluates to {@code true};
     *                     must not be {@code null}.
     * @return a new {@code Sequence} instance configured with the specified stopping condition.
     * @throws NullPointerException if {@code stopWhenSeen} is {@code null}.
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull Sequence<T> loopUntil(Predicate<? super T> stopWhenSeen) {
        return new Sequence<>(streamSupplier, parallel, errorHandler, stopWhenSeen);
    }

    /* ========================== Intermediate Operations ========================== */

    public @NotNull Sequence<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return mapStream(s -> s.filter(predicate));
    }

    @Contract("_ -> new")
    public <R> @NotNull Sequence<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new Sequence<>(() -> build().map(mapper), parallel, errorHandler, null);
    }

    /**
     * Transforms the elements of this sequence by applying the provided mapping function to each element,
     * where each mapped element is itself a stream, and then flattens the resulting streams into a single sequence.
     *
     * @param <R> The type of elements in the resulting sequence.
     * @param mapper A non-null function that takes an element of type T and returns a stream of elements of type R.
     * @return A new sequence containing all the elements of the streams produced by the mapping function.
     * @throws NullPointerException If the provided mapping function is null.
     */
    @Contract("_ -> new")
    public <R> @NotNull Sequence<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new Sequence<>(() -> build().flatMap(mapper), parallel, errorHandler, null);
    }

    /**
     * Performs the given action on each element of the sequence without modifying the sequence itself.
     *
     * @param action the action to be performed on each element of the sequence; must not be null
     * @return a new sequence with elements unchanged but the action applied during traversal
     */
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

    /* ========================== Terminal Operations ========================== */

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

    /**
     * Executes the specified action for each element of the sequence asynchronously.
     * The action is performed in a separate thread using {@link CompletableFuture#runAsync(Runnable)}.
     *
     * @param action a {@code Consumer} to be executed for each element in the sequence; must not be {@code null}.
     *               The consumer defines the operation to perform on each element.
     *               If {@code action} is {@code null}, a {@link NullPointerException} will be thrown.
     * @return a {@code CompletableFuture<Void>} representing the asynchronous computation.
     *         The future completes normally when all elements have been processed, or
     *         exceptionally if an error occurs during processing.
     * @throws NullPointerException if {@code action} is {@code null}.
     */
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

    /**
     * Returns a {@code Stream<T>} constructed using the internal {@code build} method.
     * If construction fails, the error is handled using the defined error handler,
     * and an empty stream is returned as a fallback.
     *
     * @return a {@code Stream<T>} containing the elements generated by the sequence,
     *         or an empty stream if an error occurs during construction.
     */
    public Stream<T> stream() {
        return Try.of(this::build)
                .onFailure(this::handleError)
                .getOrElse(Stream.empty());
    }

    /* ========================== Internal Pipeline ========================== */

    /**
     * Applies a transformation to the stream produced by the current sequence
     * using the provided unary operator and returns a new sequence with the transformed stream.
     *
     * @param op the unary operator to apply to the stream generated by this sequence
     * @return a new sequence containing the transformed stream
     */
    @Contract(value = "_ -> new", pure = true)
    private @NotNull Sequence<T> mapStream(UnaryOperator<Stream<T>> op) {
        return new Sequence<>(() -> op.apply(build()), parallel, errorHandler, loopUntil);
    }

    /**
     * Builds and returns a Stream based on the configuration of the current object.
     * The stream can be either parallel or sequential depending on the value of the
     * {@code parallel} flag.
     *
     * @return a Stream of type T, either in parallel or sequential mode
     */
    private Stream<T> build() {
        Stream<T> s = streamSupplier.get();
        return parallel ? s.parallel() : s.sequential();
    }

    /* ========================== Loop Implementation ========================== */

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

    /* ========================== Error Handling Internals ========================== */

    private void handleError(Throwable t) {
        if (errorHandler != null) errorHandler.accept(t);
        else {
            System.err.println("[Seq] Error in pipeline:");
            t.printStackTrace();
        }
    }
}