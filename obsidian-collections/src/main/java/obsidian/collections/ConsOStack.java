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

public final class ConsOStack<E> extends AbstractList<E> implements OStack<E> {

    private static final ConsOStack<?> EMPTY = new ConsOStack<>(null, null, 0);

    private final E             head;
    private final ConsOStack<E> tail;
    private final int           size;

    private ConsOStack(E head, ConsOStack<E> tail, int size) {
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <E> ConsOStack<E> empty() {
        return (ConsOStack<E>) EMPTY;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        Objects.checkIndex(index, size);
        ConsOStack<E> cur = this;
        for (int i = 0; i < index; i++) cur = cur.tail;
        return cur.head;
    }

    @Contract("_ -> new")
    @Override
    public @NotNull OStack<E> plus(E e) {
        Objects.requireNonNull(e, "null elements are not supported");
        return new ConsOStack<>(e, this, size + 1);
    }

    @Override
    public OStack<E> plusAll(Collection<? extends E> list) {
        Objects.requireNonNull(list, "list");

        Object[] arr = list.toArray();
        OStack<E> s = this;
        for (int i = arr.length - 1; i >= 0; i--) {
            @SuppressWarnings("unchecked") E e = (E) arr[i];
            s = s.plus(e);
        }
        return s;
    }

    @Override
    public OStack<E> with(int i, E e) {
        Objects.checkIndex(i, size);
        Objects.requireNonNull(e, "null elements are not supported");
        if (i == 0) return new ConsOStack<>(e, tail, size);
        return rebuildWith(i, e, this);
    }

    private static <E> OStack<E> rebuildWith(int i, E e, ConsOStack<E> from) {

        List<E> prefix = new ArrayList<>(i);
        ConsOStack<E> cur = from;
        for (int idx = 0; idx < i; idx++) {
            prefix.add(cur.head);
            cur = cur.tail;
        }

        OStack<E> out = cur.tail;
        out = out.plus(e);
        for (int idx = prefix.size() - 1; idx >= 0; idx--) out = out.plus(prefix.get(idx));
        return out;
    }

    @Override
    public OStack<E> plus(int i, E e) {
        Objects.requireNonNull(e, "null elements are not supported");
        if (i == 0) return plus(e);
        if (i == size) {
            return plusAllAtEnd(List.of(e));
        }
        Objects.checkIndex(i, size);
        return insertAt(i, e);
    }

    private OStack<E> insertAt(int i, E e) {
        List<E> prefix = new ArrayList<>(i);
        ConsOStack<E> cur = this;
        for (int idx = 0; idx < i; idx++) {
            prefix.add(cur.head);
            cur = cur.tail;
        }
        OStack<E> out = cur;
        out = out.plus(e);
        for (int idx = prefix.size() - 1; idx >= 0; idx--) out = out.plus(prefix.get(idx));
        return out;
    }

    private OStack<E> plusAllAtEnd(@NotNull Collection<? extends E> tailItems) {
        ArrayList<E> all = new ArrayList<>(size + tailItems.size());
        all.addAll(this);
        all.addAll(tailItems);

        OStack<E> s = empty();
        for (int idx = all.size() - 1; idx >= 0; idx--) s = s.plus(all.get(idx));
        return s;
    }

    @Override
    public OStack<E> plusAll(int i, Collection<? extends E> list) {
        Objects.requireNonNull(list, "list");
        OStack<E> out = this;
        int idx = i;
        for (E e : list) {
            out = out.plus(idx, e);
            idx++;
        }
        return out;
    }

    @Override
    public OStack<E> minus(Object e) {
        if (size == 0) return this;
        ArrayList<E> all = new ArrayList<>(size);
        boolean removed = false;
        for (E x : this) {
            if (!removed && Objects.equals(x, e)) {
                removed = true;
                continue;
            }
            all.add(x);
        }
        if (!removed) return this;
        OStack<E> s = empty();
        for (int idx = all.size() - 1; idx >= 0; idx--) s = s.plus(all.get(idx));
        return s;
    }

    @Override
    public OStack<E> minusAll(Collection<?> list) {
        Objects.requireNonNull(list, "list");
        if (list.isEmpty() || size == 0) return this;
        ArrayList<E> all = new ArrayList<>(size);
        for (E x : this) if (!list.contains(x)) all.add(x);
        OStack<E> s = empty();
        for (int idx = all.size() - 1; idx >= 0; idx--) s = s.plus(all.get(idx));
        return s;
    }

    @Override
    public OStack<E> minus(int i) {
        Objects.checkIndex(i, size);
        ArrayList<E> all = new ArrayList<>(size - 1);
        for (int idx = 0; idx < size; idx++) {
            if (idx != i) all.add(get(idx));
        }
        OStack<E> s = empty();
        for (int idx = all.size() - 1; idx >= 0; idx--) s = s.plus(all.get(idx));
        return s;
    }

    @Override
    public OStack<E> subList(int start, int end) {
        Objects.checkFromToIndex(start, end, size);
        ArrayList<E> all = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) all.add(get(i));
        OStack<E> s = empty();
        for (int idx = all.size() - 1; idx >= 0; idx--) s = s.plus(all.get(idx));
        return s;
    }

    @Override
    public OStack<E> subList(int start) {
        return subList(start, size);
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

    @Deprecated
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException("immutable");
    }
}