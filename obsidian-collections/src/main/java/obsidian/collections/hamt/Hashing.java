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

package obsidian.collections.hamt;

/**
 * Hash utilities used by the HAMT (Hash Array Mapped Trie) implementation.
 *
 * <h2>Overview</h2>
 * {@code Hashing} centralizes all hash-related operations required by the
 * HAMT data structures used in {@code obsidian.collections}.
 *
 * <p>
 * Its primary responsibilities are:
 * <ul>
 *   <li>Normalizing user-provided {@link Object#hashCode()} values</li>
 *   <li>Spreading poorly-distributed hashes</li>
 *   <li>Extracting index segments for trie navigation</li>
 *   <li>Supporting bitmap-based node indexing</li>
 * </ul>
 *
 * <h2>Why mixing is required</h2>
 * Many {@code hashCode()} implementations are not uniformly distributed.
 * Poor hash distribution can lead to:
 * <ul>
 *   <li>Uneven trie depth</li>
 *   <li>Increased collisions</li>
 *   <li>Performance degradation</li>
 * </ul>
 *
 * <p>
 * To mitigate this, HAMT implementations typically apply a mixing step.
 * This class uses the finalization step of MurmurHash3, which provides
 * excellent avalanche characteristics at minimal cost.
 *
 * <h2>Bit layout</h2>
 * HAMT splits a 32-bit hash into fixed-width segments:
 * <ul>
 *   <li>5 bits per level</li>
 *   <li>32-way branching factor</li>
 * </ul>
 *
 * <p>
 * Each trie level consumes one segment extracted via {@link #mask(int, int)}.
 *
 * <h2>Bitmap indexing</h2>
 * Internal nodes use bitmaps to compactly represent sparse children.
 * {@link #bitShift(int)} and {@link #index(int, int)} support fast translation
 * from logical position to physical array index.
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>This class is purely static and allocation-free</li>
 *   <li>All operations are deterministic</li>
 *   <li>No external hashing libraries are required</li>
 * </ul>
 *
 * @see obsidian.collections.hamt
 */
public final class Hashing {

    private Hashing() {
    }

    /**
     * Computes a normalized hash value for the given object.
     *
     * <p>
     * This method:
     * <ul>
     *   <li>Handles {@code null} values safely</li>
     *   <li>Applies a mixing step to improve distribution</li>
     * </ul>
     *
     * @param o the object to hash; may be {@code null}
     * @return a mixed 32-bit hash value
     */
    public static int hash(Object o) {
        return mix(o == null ? 0 : o.hashCode());
    }

    /**
     * Applies a MurmurHash3 finalization mix to the given hash value.
     *
     * <p>
     * This function provides strong avalanche behavior, ensuring that
     * small differences in input hash values result in large differences
     * in output.
     *
     * <p>
     * The implementation is derived from the MurmurHash3 32-bit finalizer
     * and is considered "good enough" for HAMT usage.
     *
     * @param h the raw hash value
     * @return the mixed hash value
     */
    public static int mix(int h) {
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);
        return h;
    }

    /**
     * Extracts a 5-bit segment from the hash at the given shift offset.
     *
     * <p>
     * This corresponds to one level of HAMT traversal and yields a value
     * in the range {@code [0, 31]}.
     *
     * @param hash  the mixed hash value
     * @param shift the bit offset (must be a multiple of 5)
     * @return the 5-bit segment for the current trie level
     */
    public static int mask(int hash, int shift) {
        return (hash >>> shift) & 0x1f; // 5 bits (0..31)
    }

    /**
     * Converts a 5-bit mask value into a bitmap with a single bit set.
     *
     * <p>
     * This is used to represent the presence of a child node in a bitmap-indexed
     * HAMT node.
     *
     * @param mask the 5-bit mask (0–31)
     * @return an integer with exactly one bit set
     */
    public static int bitShift(int mask) {
        return 1 << mask;
    }

    /**
     * Computes the physical array index for a child node based on a bitmap.
     *
     * <p>
     * This method counts the number of bits set in {@code bitmap} that are
     * positioned before {@code bitshift}.
     *
     * <p>
     * This allows HAMT nodes to store children densely while maintaining
     * logical ordering.
     *
     * @param bitmap   the bitmap representing present children
     * @param bitshift the single-bit mask of the desired child
     * @return the index into the compact child array
     */
    public static int index(int bitmap, int bitshift) {
        return Integer.bitCount(bitmap & (bitshift - 1));
    }
}