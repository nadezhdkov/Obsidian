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

package obsidian.collections;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Persistent (immutable) vector implementation backed by fixed-size chunks.
 *
 * <h2>Overview</h2>
 * {@code ChunkedOVector} is an immutable random-access sequence similar to {@link ArrayList},
 * but persistent: all "mutating" operations return a new vector while sharing as much structure
 * as possible with the original instance.
 *
 * <p>
 * Internally, elements are stored in a two-dimensional array ({@code Object[][] chunks}),
 * where each inner array is a fixed-size chunk of {@code 32} elements.
 *
 * <h2>Chunk layout</h2>
 * The vector splits indices using bit operations:
 * <ul>
 *   <li>{@code chunkIndex = index >> 5} (divide by 32)</li>
 *   <li>{@code offset     = index & 31} (mod 32)</li>
 * </ul>
 *
 * <p>
 * This yields:
 * <ul>
 *   <li>Fast {@link #get(int)}: {@code O(1)}</li>
 *   <li>Fast append {@link #plus(Object)}: typically {@code O(1)} with minimal copying</li>
 *   <li>Fast updates {@link #with(int, Object)}: {@code O(1)} with copy-on-write of one chunk</li>
 * </ul>
 *
 * <h2>Structural sharing</h2>
 * When appending or updating, this structure copies only:
 * <ul>
 *   <li>the outer chunks array when it needs to grow</li>
 *   <li>the modified chunk (32-sized array)</li>
 * </ul>
 *
 * <p>
 * All other chunks are shared with previous versions of the vector.
 *
 * <h2>Complexity</h2>
 * <ul>
 *   <li>{@link #get(int)}: {@code O(1)}</li>
 *   <li>{@link #plus(Object)} (append): amortized {@code O(1)}</li>
 *   <li>{@link #with(int, Object)}: {@code O(1)}</li>
 *   <li>{@link #subList(int, int)}: {@code O(n)} (materializes a new vector)</li>
 *   <li>{@link #plus(int, Object)} / {@link #minus(int)}: {@code O(n)} (rebuilds via list)</li>
 * </ul>
 *
 * <h2>Null policy</h2>
 * {@code null} elements are not supported. Any attempt to insert {@code null} throws
 * {@link NullPointerException}.
 *
 * <h2>Interoperability with {@link List}</h2>
 * This class extends {@link AbstractList} for interoperability and provides read-only list behavior.
 * Standard mutators from {@link List} are overridden and throw {@link UnsupportedOperationException}.
 *
 * @param <E> the element type
 *
 * @see OVector
 * @see Empty#vector()
 */
public final class ChunkedOVector<E> extends AbstractList<E> implements OVector<E> {

    /** Number of bits used per chunk (32 elements). */
    private static final int CHUNK_SHIFT = 5;          // 2^5 = 32

    /** Fixed chunk size (32). */
    private static final int CHUNK_SIZE  = 1 << CHUNK_SHIFT;

    /** Mask used to compute the index within a chunk (0..31). */
    private static final int CHUNK_MASK  = CHUNK_SIZE - 1;

    private static final ChunkedOVector<?> EMPTY = new ChunkedOVector<>(new Object[0][], 0);

    private final Object[][] chunks;
    private final int        size;

    private ChunkedOVector(Object[][] chunks, int size) {
        this.chunks = chunks;
        this.size   = size;
    }

    /**
     * Returns the canonical empty vector instance.
     *
     * <p>
     * The returned instance is shared and allocation-free.
     */
    @SuppressWarnings("unchecked")
    public static <E> ChunkedOVector<E> empty() {
        return (ChunkedOVector<E>) EMPTY;
    }

    /**
     * Returns the number of elements currently stored in this vector.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns the element at the specified index.
     *
     * <p>
     * This operation is {@code O(1)} and performs a chunk lookup using bit operations.
     *
     * @param index the element index (0-based)
     * @return the element at {@code index}
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        Objects.checkIndex(index, size);
        int ci  = index >> CHUNK_SHIFT;
        int off = index & CHUNK_MASK;
        return (E) chunks[ci][off];
    }

    /**
     * Returns a new vector with {@code e} appended at the end.
     *
     * <p>
     * This operation performs copy-on-write on the last chunk (or allocates a new chunk if needed),
     * and may grow the outer chunks array when crossing a chunk boundary.
     *
     * <p>
     * Amortized complexity: {@code O(1)}.
     *
     * @param e the element to append (must not be {@code null})
     * @return a new vector containing the appended element
     * @throws NullPointerException if {@code e} is {@code null}
     */
    @Contract("_ -> new")
    @Override
    public @NotNull OVector<E> plus(E e) {
        Objects.requireNonNull(e, "null elements are not supported");

        int newSize = size + 1;
        int neededChunks = chunkCountForSize(newSize);

        Object[][] newChunks = chunks;
        if (neededChunks != chunks.length) {
            newChunks = Arrays.copyOf(chunks, neededChunks);
        }

        int lastIndex = newSize - 1;
        int ci = lastIndex >> CHUNK_SHIFT;
        int off = lastIndex & CHUNK_MASK;

        Object[] chunk = (ci < chunks.length) ? chunks[ci] : null;

        if (chunk == null) {
            chunk = new Object[CHUNK_SIZE];
        } else if (off == 0) {
            chunk = new Object[CHUNK_SIZE];
        } else {
            chunk = Arrays.copyOf(chunk, CHUNK_SIZE);
        }

        chunk[off] = e;
        newChunks[ci] = chunk;

        return new ChunkedOVector<>(newChunks, newSize);
    }

    /**
     * Returns a new vector containing all elements from {@code list} appended in encounter order.
     *
     * @param list the elements to append
     * @return a new vector containing the appended elements
     * @throws NullPointerException if {@code list} is null or contains null elements
     */
    @Override
    public OVector<E> plusAll(Collection<? extends E> list) {
        Objects.requireNonNull(list, "list");
        OVector<E> out = this;
        for (E e : list) out = (OVector<E>) out.plus(e);
        return out;
    }

    /**
     * Returns a new vector equal to this one, but with the element at {@code index}
     * replaced by {@code value}.
     *
     * <p>
     * This operation copies only:
     * <ul>
     *   <li>the outer chunks array (shallow copy)</li>
     *   <li>the affected chunk (32-sized copy)</li>
     * </ul>
     *
     * <p>
     * Complexity: {@code O(1)}.
     *
     * @param index the element index to replace
     * @param value the new value (must not be {@code null})
     * @return a new vector with the updated element
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Contract("_, _ -> new")
    @Override
    public @NotNull OVector<E> with(int index, E value) {
        Objects.checkIndex(index, size);
        Objects.requireNonNull(value, "null elements are not supported");

        int ci = index >> CHUNK_SHIFT;
        int off = index & CHUNK_MASK;

        Object[][] newChunks = Arrays.copyOf(chunks, chunks.length);
        Object[] newChunk = Arrays.copyOf(chunks[ci], CHUNK_SIZE);
        newChunk[off] = value;
        newChunks[ci] = newChunk;

        return new ChunkedOVector<>(newChunks, size);
    }

    /**
     * Returns a new vector with {@code value} inserted at {@code index}.
     *
     * <p>
     * If {@code index == size()}, this behaves like {@link #plus(Object)} (append).
     *
     * <p>
     * Current implementation materializes the vector into a temporary list and rebuilds,
     * resulting in {@code O(n)} complexity.
     *
     * @param index insertion index (0..size)
     * @param value element to insert (must not be {@code null})
     * @return a new vector with the inserted element
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     * @throws NullPointerException if {@code value} is {@code null}
     */
    @Override
    public OVector<E> plus(int index, E value) {
        Objects.requireNonNull(value, "null elements are not supported");
        Objects.checkIndex(index, size + 1);

        if (index == size) return plus(value);

        ArrayList<E> tmp = new ArrayList<>(size + 1);
        for (int i = 0; i < index; i++) tmp.add(get(i));
        tmp.add(value);
        for (int i = index; i < size; i++) tmp.add(get(i));

        return fromList(tmp);
    }

    @Override
    public OVector<E> plusAll(int index, Collection<? extends E> list) {
        Objects.requireNonNull(list, "list");
        Objects.checkIndex(index, size + 1);

        OVector<E> out = this;
        int i = index;
        for (E e : list) {
            out = (OVector<E>) out.plus(i, e);
            i++;
        }
        return out;
    }

    /**
     * Returns a new vector with the first occurrence of {@code e} removed.
     *
     * <p>
     * If the element is not present, returns {@code this}.
     * Complexity is {@code O(n)} due to rebuilding.
     */
    @Override
    public OVector<E> minus(Object e) {
        int idx = indexOf(e);
        if (idx < 0) return this;
        return minus(idx);
    }

    /**
     * Returns a new vector with the element at {@code index} removed.
     *
     * <p>
     * Current implementation rebuilds the vector, resulting in {@code O(n)} complexity.
     */
    @Override
    public OVector<E> minus(int index) {
        Objects.checkIndex(index, size);
        ArrayList<E> tmp = new ArrayList<>(size - 1);
        for (int i = 0; i < size; i++) if (i != index) tmp.add(get(i));
        return fromList(tmp);
    }

    /**
     * Returns a new vector containing only elements not present in {@code list}.
     *
     * <p>
     * Complexity: {@code O(n)}.
     *
     * @throws NullPointerException if {@code list} is null
     */
    @Override
    public OVector<E> minusAll(Collection<?> list) {
        Objects.requireNonNull(list, "list");
        if (list.isEmpty() || size == 0) return this;

        ArrayList<E> tmp = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            E v = get(i);
            if (!list.contains(v)) tmp.add(v);
        }
        return fromList(tmp);
    }

    /**
     * Returns a persistent vector containing elements in {@code [fromIndex, toIndex)}.
     *
     * <p>
     * Unlike {@link List#subList(int, int)}, this method returns a new persistent vector
     * (not a view). The result is materialized and therefore {@code O(n)}.
     */
    @Override
    public @NotNull OVector<E> subList(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, size);
        ArrayList<E> tmp = new ArrayList<>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) tmp.add(get(i));
        return fromList(tmp);
    }

    private static int chunkCountForSize(int size) {
        if (size == 0) return 0;
        return ((size - 1) >> CHUNK_SHIFT) + 1;
    }

    /**
     * Builds a {@code ChunkedOVector} from an existing list.
     *
     * <p>
     * The list is read in encounter order and copied into fixed-size chunks.
     *
     * @param list the source list
     * @return a persistent vector containing the same elements
     * @throws NullPointerException if {@code list} is null or contains null elements
     */
    public static <E> ChunkedOVector<E> fromList(List<? extends E> list) {
        Objects.requireNonNull(list, "list");
        if (list.isEmpty()) return empty();

        int size = list.size();
        int chunks = chunkCountForSize(size);
        Object[][] arr = new Object[chunks][];

        for (int ci = 0; ci < chunks; ci++) {
            Object[] chunk = new Object[CHUNK_SIZE];
            int base = ci * CHUNK_SIZE;
            int limit = Math.min(base + CHUNK_SIZE, size);
            for (int i = base; i < limit; i++) {
                Object v = list.get(i);
                if (v == null) throw new NullPointerException("null elements are not supported");
                chunk[i - base] = v;
            }
            arr[ci] = chunk;
        }

        return new ChunkedOVector<>(arr, size);
    }

    @Deprecated
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException("immutable");
    }
}