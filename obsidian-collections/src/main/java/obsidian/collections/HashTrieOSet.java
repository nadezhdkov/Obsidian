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

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class HashTrieOSet<E> extends AbstractSet<E> implements OSet<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Object          PRESENT = new Object();
    private static final HashTrieOSet<?> EMPTY   = new HashTrieOSet<>(HashTriePMap.empty());

    private final HashTriePMap<E, Object> map;

    private HashTrieOSet(HashTriePMap<E, Object> map) {
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    public static <E> HashTrieOSet<E> empty() {
        return (HashTrieOSet<E>) EMPTY;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        Iterator<Map.Entry<E, Object>> it = map.entrySet().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next().getKey();
            }
        };
    }

    @Override
    public HashTrieOSet<E> plus(E e) {
        HashTriePMap<E, Object> m2 = map.plus(e, PRESENT);
        return m2 == map ? this : new HashTrieOSet<>(m2);
    }

    @Override
    public HashTrieOSet<E> plusAll(Collection<? extends E> list) {
        Objects.requireNonNull(list, "list");
        HashTrieOSet<E> out = this;
        for (E e : list) out = out.plus(e);
        return out;
    }

    @Override
    public HashTrieOSet<E> minus(Object e) {
        HashTriePMap<E, Object> m2 = map.minus(e);
        return m2 == map ? this : new HashTrieOSet<>(m2);
    }

    @Override
    public HashTrieOSet<E> minusAll(Collection<?> list) {
        Objects.requireNonNull(list, "list");
        HashTrieOSet<E> out = this;
        for (Object e : list) out = out.minus(e);
        return out;
    }

    /**
     * Unsupported: this collection is immutable.
     *
     * @throws UnsupportedOperationException always
     * @deprecated use {@link #plus(Object)} instead
     */

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
}
