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

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

public final class TreeOSet<E> extends AbstractSet<E> implements OSortedSet<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NavigableSet<E>       set;
    private final Comparator<? super E> comparator;

    public TreeOSet(NavigableSet<E> set, Comparator<? super E> comparator) {
        this.set = Collections.unmodifiableNavigableSet(requireNonNull(set, "set"));
        this.comparator = requireNonNull(comparator, "comparator");
    }

    @Contract(" -> new")
    public static <E extends Comparable<? super E>> @NotNull TreeOSet<E> empty() {
        return empty(Comparator.naturalOrder());
    }

    @Contract("_ -> new")
    public static <E> @NotNull TreeOSet<E> empty(Comparator<? super E> comparator) {
        requireNonNull(comparator, "comparator");
        return new TreeOSet<>(new TreeSet<>(comparator), comparator);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return set.iterator();
    }

    @Override
    public TreeOSet<E> plus(E e) {
        requireNonNull(e, "element is null");
        if (set.contains(e)) return this;

        TreeSet<E> ts = new TreeSet<>(comparator);
        ts.addAll(set);
        ts.add(e);
        return new TreeOSet<>(ts, comparator);
    }

    @Override
    public TreeOSet<E> plusAll(Collection<? extends E> list) {
        requireNonNull(list, "list");
        if (list.isEmpty()) return this;

        TreeSet<E> ts = new TreeSet<>(comparator);
        ts.addAll(set);
        boolean changed = false;
        for (E e : list) {
            requireNonNull(e, "list contains null");
            changed |= ts.add(e);
        }
        return changed ? new TreeOSet<>(ts, comparator) : this;
    }

    @Override
    public TreeOSet<E> minus(Object e) {
        requireNonNull(e, "element is null");
        if (!set.contains(e)) return this;

        TreeSet<E> ts = new TreeSet<>(comparator);
        ts.addAll(set);
        ts.remove(e);
        return new TreeOSet<>(ts, comparator);
    }

    @Override
    public TreeOSet<E> minusAll(Collection<?> list) {
        requireNonNull(list, "list");
        if (list.isEmpty()) return this;

        TreeSet<E> ts = new TreeSet<>(comparator);
        ts.addAll(set);
        boolean changed = ts.removeAll(list);
        return changed ? new TreeOSet<>(ts, comparator) : this;
    }

    @Override
    public @NotNull OSortedSet<E> descendingSet() {
        return new TreeOSet<>(set.descendingSet(), comparator.reversed());
    }

    @Deprecated
    @Override
    public @NotNull Iterator<E> descendingIterator() {
        return null;
    }

    @Override
    public E lower(E e) {
        return set.lower(e);
    }

    @Override
    public E floor(E e) {
        return set.floor(e);
    }

    @Override
    public E ceiling(E e) {
        return set.ceiling(e);
    }

    @Override
    public E higher(E e) {
        return set.higher(e);
    }

    @Override
    public E first() {
        return set.first();
    }

    @Override
    public E last() {
        return set.last();
    }

    @Override
    public @NotNull NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return set.subSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public @NotNull NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return set.headSet(toElement, inclusive);
    }

    @Override
    public @NotNull NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return set.tailSet(fromElement, inclusive);
    }

    @Deprecated
    @Override
    public @NotNull SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    @Deprecated
    @Override
    public @NotNull SortedSet<E> headSet(E toElement) {
        return null;
    }

    @Deprecated
    @Override
    public @NotNull SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("immutable");
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
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("immutable");
    }
}