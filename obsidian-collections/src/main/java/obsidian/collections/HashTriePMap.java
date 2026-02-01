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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import obsidian.collections.hamt.Hashing;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.io.Serializable;
import java.io.Serial;
import java.util.*;

/**
 * Persistent (immutable) hash map implemented as a HAMT (Hash Array Mapped Trie).
 *
 * <h2>Overview</h2>
 * {@code HashTriePMap} is an immutable map that provides efficient lookups and updates while
 * preserving previous versions through structural sharing.
 *
 * <p>
 * Internally this map is backed by a HAMT:
 * <ul>
 *   <li>Keys are hashed (and mixed) using {@link Hashing}</li>
 *   <li>The 32-bit hash is traversed in 5-bit segments (32-way branching)</li>
 *   <li>Sparse children are stored compactly using a bitmap + dense array</li>
 * </ul>
 *
 * <h2>Persistence and structural sharing</h2>
 * All "mutating" operations return a <b>new</b> map instance:
 * <ul>
 *   <li>{@link #plus(Object, Object)} returns a new map with the entry added/replaced</li>
 *   <li>{@link #minus(Object)} returns a new map with the key removed</li>
 * </ul>
 *
 * <p>
 * Only the nodes along the modified path are copied. Unchanged subtrees are reused across
 * versions, making updates cheap and safe for sharing across threads.
 *
 * <h2>Hash normalization</h2>
 * To protect the trie against weak {@link Object#hashCode()} implementations, this map uses
 * {@link Hashing#hash(Object)} (which applies a Murmur3-style mix). This improves distribution
 * and reduces collision hotspots.
 *
 * <h2>Complexity</h2>
 * Typical time complexity (amortized/expected):
 * <ul>
 *   <li>{@link #get(Object)}: {@code O(1)} expected</li>
 *   <li>{@link #containsKey(Object)}: {@code O(1)} expected</li>
 *   <li>{@link #plus(Object, Object)}: {@code O(1)} expected</li>
 *   <li>{@link #minus(Object)}: {@code O(1)} expected</li>
 * </ul>
 *
 * <p>
 * Worst case can degrade if many keys collide into the same hash, in which case a
 * {@code CollisionNode} stores multiple entries linearly.
 *
 * <h2>Iteration</h2>
 * {@link #entrySet()} returns a read-only snapshot view. The iterator materializes
 * entries into a list for predictable iteration over an immutable map.
 *
 * <h2>Interoperability with {@link Map}</h2>
 * This class extends {@link AbstractMap} for compatibility, but is not mutable.
 * Standard mutators such as {@link #put(Object, Object)} are deprecated and throw
 * {@link UnsupportedOperationException}.
 *
 * @param <K> key type
 * @param <V> value type
 *
 * @see OMap
 * @see Hashing
 */
public final class HashTriePMap<K, V> extends AbstractMap<K, V> implements OMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final HashTriePMap<?, ?> EMPTY = new HashTriePMap<>(Node.empty(), 0);

    private final Node<K, V> root;
    private final int size;

    private HashTriePMap(Node<K, V> root, int size) {
        this.root = root;
        this.size = size;
    }

    /**
     * Returns the canonical empty persistent map.
     *
     * <p>
     * The returned instance is shared and allocation-free.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashTriePMap<K, V> empty() {
        return (HashTriePMap<K, V>) EMPTY;
    }

    /**
     * Returns the number of key-value pairs in this map.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this map contains a mapping for {@code key}.
     *
     * <p>
     * Key lookup uses a mixed hash via {@link Hashing#hash(Object)} and navigates
     * the HAMT structure using 5-bit segments per level.
     *
     * @param key the key to check (may be {@code null})
     * @return {@code true} if the key exists, otherwise {@code false}
     */
    @Override
    public boolean containsKey(Object key) {
        return root.get(Hashing.hash(key), key, 0) != Node.NOT_FOUND;
    }

    /**
     * Returns the value associated with {@code key}, or {@code null} if not present.
     *
     * <p>
     * Note: as in {@link Map#get(Object)}, returning {@code null} does not distinguish
     * between "missing key" and "key mapped to null". This implementation allows
     * {@code null} values, so callers needing that distinction should use
     * {@link #containsKey(Object)} as well.
     *
     * @param key the key to lookup (may be {@code null})
     * @return the associated value, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    @Override
    public @Nullable V get(Object key) {
        Object v = root.get(Hashing.hash(key), key, 0);
        return v == Node.NOT_FOUND ? null : (V) v;
    }

    /**
     * Returns a new map with {@code key} associated to {@code value}.
     *
     * <p>
     * If {@code key} already exists, its value is replaced.
     * If the map would not change (same key/value), this returns {@code this}.
     *
     * <p>
     * This operation is persistent: only nodes on the updated path are copied;
     * all other nodes are shared.
     *
     * @param key the key (may be {@code null})
     * @param value the value (may be {@code null})
     * @return a new map reflecting the update, or {@code this} if no change occurred
     */
    @Override
    public HashTriePMap<K, V> plus(K key, V value) {
        int h = Hashing.hash(key);
        Box added = new Box(false);
        Node<K, V> newRoot = root.put(h, key, value, 0, added);
        if (newRoot == root) return this;
        return new HashTriePMap<>(newRoot, added.value ? size + 1 : size);
    }

    /**
     * Returns a new map containing all entries of {@code map} added to this map.
     *
     * <p>
     * Entries are applied in the iteration order of {@code map.entrySet()}.
     *
     * @param map entries to add
     * @return a new map containing the merged entries
     * @throws NullPointerException if {@code map} is null
     */
    @Override
    public HashTriePMap<K, V> plusAll(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map");
        HashTriePMap<K, V> out = this;
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            out = out.plus(e.getKey(), e.getValue());
        }
        return out;
    }

    /**
     * Returns a new map with {@code key} removed.
     *
     * <p>
     * If {@code key} is not present, returns {@code this}.
     *
     * @param key the key to remove (may be {@code null})
     * @return a new map without the given key, or {@code this} if not present
     */
    @Override
    public HashTriePMap<K, V> minus(Object key) {
        int h = Hashing.hash(key);
        Box removed = new Box(false);
        Node<K, V> newRoot = root.remove(h, key, 0, removed);
        if (!removed.value) return this;
        return new HashTriePMap<>(newRoot, size - 1);
    }

    /**
     * Returns a new map with all keys contained in {@code keys} removed.
     *
     * @param keys the keys to remove
     * @return a new map without those keys
     * @throws NullPointerException if {@code keys} is null
     */
    @Override
    public HashTriePMap<K, V> minusAll(Collection<?> keys) {
        Objects.requireNonNull(keys, "keys");
        HashTriePMap<K, V> out = this;
        for (Object k : keys) out = out.minus(k);
        return out;
    }

    private transient Set<Entry<K, V>> entrySet;

    /**
     * Returns a read-only {@link Set} view of the map entries.
     *
     * <p>
     * The returned set is cached per map instance (transient) and its iterator
     * materializes the entries into a list snapshot.
     *
     * <p>
     * This is consistent with immutability: iterating is safe and does not observe
     * concurrent mutation (because no mutation exists).
     *
     * @return a read-only set of entries
     */
    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet<>() {
                @Override
                public int size() {
                    return HashTriePMap.this.size;
                }

                @Override
                public @NotNull Iterator<Entry<K, V>> iterator() {
                    ArrayList<Entry<K, V>> out = new ArrayList<>(HashTriePMap.this.size);
                    root.forEach(out::add);
                    return Collections.unmodifiableList(out).iterator();
                }

                @Override
                public boolean contains(Object o) {
                    if (!(o instanceof Entry<?, ?> e)) return false;
                    Object k = e.getKey();
                    if (!HashTriePMap.this.containsKey(k)) return false;
                    return Objects.equals(HashTriePMap.this.get(k), e.getValue());
                }
            };
        }
        return entrySet;
    }

    /**
     * Performs the given action for each key-value pair in this map.
     *
     * <p>
     * Traversal is implemented by visiting all HAMT nodes and emitting entries.
     *
     * @param action the action to perform for each entry
     * @throws NullPointerException if {@code action} is null
     */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action, "action");
        root.forEach(e -> action.accept(e.getKey(), e.getValue()));
    }

    @Deprecated
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("immutable");
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException("immutable");
    }

    private static final class Box {
        boolean value;

        Box(boolean v) {
            this.value = v;
        }
    }

    /**
     * Internal HAMT node abstraction.
     *
     * <p>
     * Node types:
     * <ul>
     *   <li>{@code EmptyNode}: represents absence of mappings</li>
     *   <li>{@code LeafNode}: stores a single key/value pair</li>
     *   <li>{@code CollisionNode}: stores multiple entries with the same full hash</li>
     *   <li>{@code BitmapIndexedNode}: main branching node using bitmap + dense children array</li>
     * </ul>
     *
     * <p>
     * Navigation uses {@code shift} to select successive 5-bit hash segments.
     */
    private interface Node<K, V> {
        Object NOT_FOUND = new Object();

        Object     get(int hash, Object key, int shift);

        Node<K, V> put(int hash, K key, V value, int shift, Box added);

        Node<K, V> remove(int hash, Object key, int shift, Box removed);

        void forEach(Consumer<Entry<K, V>> sink);

        static <K, V> Node<K, V> empty() {
            return EmptyNode.instance();
        }

        default Node<K, V> putAllCollision(@NotNull CollisionNode<K, V> col, int shift) {
            Node<K, V> n = this;
            for (int i = 0; i < col.keys.length; i++) {
                @SuppressWarnings("unchecked") K k = (K) col.keys[i];
                @SuppressWarnings("unchecked") V v = (V) col.values[i];
                n = n.put(col.hash, k, v, shift, new Box(false));
            }
            return n;
        }

    }

    private static final class EmptyNode<K, V> implements Node<K, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
        private static final EmptyNode<?, ?> INSTANCE = new EmptyNode<>();

        @SuppressWarnings("unchecked")
        static <K, V> EmptyNode<K, V> instance() {
            return (EmptyNode<K, V>) INSTANCE;
        }

        @Override
        public Object get(int hash, Object key, int shift) {
            return NOT_FOUND;
        }

        @Contract("_, _, _, _, _ -> new")
        @Override
        public @NotNull Node<K, V> put(int hash, K key, V value, int shift, @NotNull Box added) {
            added.value = true;
            return new LeafNode<>(hash, key, value);
        }

        @Override
        public Node<K, V> remove(int hash, Object key, int shift, Box removed) {
            return this;
        }

        @Override
        public void forEach(java.util.function.Consumer<Entry<K, V>> sink) { /* none */ }
    }

    private record LeafNode<K, V>(int hash, K key, V value) implements Node<K, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Object get(int hash, Object key, int shift) {
            if (this.hash == hash && Objects.equals(this.key, key)) return value;
            return NOT_FOUND;
        }

        @Override
        public Node<K, V> put(int hash, K key, V value, int shift, Box added) {
            if (this.hash == hash) {
                if (Objects.equals(this.key, key)) {
                    if (Objects.equals(this.value, value)) return this;
                    return new LeafNode<>(hash, key, value);
                }

                added.value = true;
                return new CollisionNode<>(hash,
                        new Object[]{this.key, key},
                        new Object[]{this.value, value});
            }

            added.value = true;
            return BitmapIndexedNode.mergeLeaves(this, new LeafNode<>(hash, key, value), shift);
        }

        @Override
        public Node<K, V> remove(int hash, Object key, int shift, Box removed) {
            if (this.hash == hash && Objects.equals(this.key, key)) {
                removed.value = true;
                return EmptyNode.instance();
            }
            return this;
        }

        @Override
        public void forEach(@NotNull Consumer<Entry<K, V>> sink) {
            sink.accept(new SimpleImmutableEntry<>(key, value));
        }
    }

    private record CollisionNode<K, V>(int hash, Object[] keys, Object[] values) implements Node<K, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Object get(int hash, Object key, int shift) {
            if (this.hash != hash) return NOT_FOUND;
            for (int i = 0; i < keys.length; i++) {
                if (Objects.equals(keys[i], key)) return values[i];
            }
            return NOT_FOUND;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Node<K, V> put(int hash, K key, V value, int shift, Box added) {
            if (this.hash != hash) {
                added.value = true;
                return BitmapIndexedNode.mergeLeaves(
                                new LeafNode<>(this.hash, (K) keys[0], (V) values[0]),
                                new LeafNode<>(hash, key, value),
                                shift
                        ).put(this.hash, (K) keys[1], (V) values[1], shift, new Box(false))
                        .putAllCollision(this, shift);
            }

            for (int i = 0; i < keys.length; i++) {
                if (Objects.equals(keys[i], key)) {
                    if (Objects.equals(values[i], value)) return this;
                    Object[] nk = keys.clone();
                    Object[] nv = values.clone();
                    nk[i] = key;
                    nv[i] = value;
                    return new CollisionNode<>(hash, nk, nv);
                }
            }
            added.value = true;
            Object[] nk = Arrays.copyOf(keys, keys.length + 1);
            Object[] nv = Arrays.copyOf(values, values.length + 1);
            nk[keys.length] = key;
            nv[values.length] = value;
            return new CollisionNode<>(hash, nk, nv);
        }

        @Override
        public Node<K, V> remove(int hash, Object key, int shift, Box removed) {
            if (this.hash != hash) return this;

            int idx = -1;
            for (int i = 0; i < keys.length; i++) {
                if (Objects.equals(keys[i], key)) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) return this;

            removed.value = true;

            if (keys.length == 2) {
                int other = idx == 0 ? 1 : 0;
                @SuppressWarnings("unchecked") K ok = (K) keys[other];
                @SuppressWarnings("unchecked") V ov = (V) values[other];
                return new LeafNode<>(hash, ok, ov);
            }

            Object[] nk = new Object[keys.length - 1];
            Object[] nv = new Object[values.length - 1];
            int p = 0;
            for (int i = 0; i < keys.length; i++) {
                if (i == idx) continue;
                nk[p] = keys[i];
                nv[p] = values[i];
                p++;
            }
            return new CollisionNode<>(hash, nk, nv);
        }

        @Override
        public void forEach(Consumer<Entry<K, V>> sink) {
            for (int i = 0; i < keys.length; i++) {
                @SuppressWarnings("unchecked") K k = (K) keys[i];
                @SuppressWarnings("unchecked") V v = (V) values[i];
                sink.accept(new SimpleImmutableEntry<>(k, v));
            }
        }

    }

    /**
     * Bitmap-indexed branching node.
     *
     * <p>
     * {@code bitmap} encodes which of the 32 possible child slots exist at this level.
     * Children are stored densely in {@code children}, and {@link Hashing#index(int, int)}
     * translates a logical bit position into a physical array index.
     */
    private record BitmapIndexedNode<K, V>(int bitmap, Node<K, V>[] children) implements Node<K, V>, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Object get(int hash, Object key, int shift) {
            int m = Hashing.mask(hash, shift);
            int bit = Hashing.bitShift(m);
            if ((bitmap & bit) == 0) return NOT_FOUND;
            int idx = Hashing.index(bitmap, bit);
            return children[idx].get(hash, key, shift + 5);
        }

        @Override
        public Node<K, V> put(int hash, K key, V value, int shift, Box added) {
            int m = Hashing.mask(hash, shift);
            int bit = Hashing.bitShift(m);
            int idx = Hashing.index(bitmap, bit);

            if ((bitmap & bit) == 0) {
                @SuppressWarnings("unchecked")
                Node<K, V>[] newChildren = (Node<K, V>[]) new Node[children.length + 1];
                System.arraycopy(children, 0, newChildren, 0, idx);
                newChildren[idx] = new LeafNode<>(hash, key, value);
                System.arraycopy(children, idx, newChildren, idx + 1, children.length - idx);
                added.value = true;
                return new BitmapIndexedNode<>(bitmap | bit, newChildren);
            }

            Node<K, V> child = children[idx];
            Node<K, V> newChild = child.put(hash, key, value, shift + 5, added);
            if (newChild == child) return this;

            Node<K, V>[] newChildren = children.clone();
            newChildren[idx] = newChild;
            return new BitmapIndexedNode<>(bitmap, newChildren);
        }

        @Override
        public Node<K, V> remove(int hash, Object key, int shift, Box removed) {
            int m = Hashing.mask(hash, shift);
            int bit = Hashing.bitShift(m);
            if ((bitmap & bit) == 0) return this;

            int idx = Hashing.index(bitmap, bit);
            Node<K, V> child = children[idx];
            Node<K, V> newChild = child.remove(hash, key, shift + 5, removed);
            if (newChild == child) return this;

            if (newChild instanceof EmptyNode) {
                int newBitmap = bitmap & ~bit;
                if (newBitmap == 0) return EmptyNode.instance();

                @SuppressWarnings("unchecked")
                Node<K, V>[] newChildren = (Node<K, V>[]) new Node[children.length - 1];
                System.arraycopy(children, 0, newChildren, 0, idx);
                System.arraycopy(children, idx + 1, newChildren, idx, children.length - idx - 1);

                if (newChildren.length == 1 && newChildren[0] instanceof LeafNode<?, ?> leaf) {
                    @SuppressWarnings("unchecked") Node<K, V> one = (Node<K, V>) leaf;
                    return one;
                }
                return new BitmapIndexedNode<>(newBitmap, newChildren);
            } else {
                Node<K, V>[] newChildren = children.clone();
                newChildren[idx] = newChild;
                return new BitmapIndexedNode<>(bitmap, newChildren);
            }
        }

        @Override
        public void forEach(Consumer<Entry<K, V>> sink) {
            for (Node<K, V> c : children) c.forEach(sink);
        }

        @Contract("_, _, _ -> new")
        static <K, V> @NotNull Node<K, V> mergeLeaves(@NotNull LeafNode<K, V> a, @NotNull LeafNode<K, V> b, int shift) {
            int am = Hashing.mask(a.hash, shift);
            int bm = Hashing.mask(b.hash, shift);
            int abit = Hashing.bitShift(am);
            int bbit = Hashing.bitShift(bm);

            if (am != bm) {
                @SuppressWarnings("unchecked")
                Node<K, V>[] kids = (Node<K, V>[]) new Node[2];
                int bitmap = abit | bbit;

                if (am < bm) {
                    kids[0] = a;
                    kids[1] = b;
                } else {
                    kids[0] = b;
                    kids[1] = a;
                }
                return new BitmapIndexedNode<>(bitmap, kids);
            }

            Node<K, V> sub = mergeLeaves(a, b, shift + 5);
            @SuppressWarnings("unchecked")
            Node<K, V>[] kids = (Node<K, V>[]) new Node[]{sub};
            return new BitmapIndexedNode<>(abit, kids);
        }
    }
}