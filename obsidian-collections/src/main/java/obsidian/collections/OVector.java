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

/**
 * Persistent (immutable) vector abstraction.
 *
 * <h2>Overview</h2>
 * {@code OVector} represents an immutable, random-access sequence of elements.
 * It behaves similarly to {@link java.util.List} / {@link java.util.ArrayList} for reads,
 * but all update operations (when provided by implementations) return new vectors instead of
 * modifying the current instance.
 *
 * <h2>Key properties</h2>
 * <ul>
 *   <li><b>Persistent</b>: operations produce new instances, enabling structural sharing.</li>
 *   <li><b>Random access</b>: implementations are expected to provide efficient {@code get(index)}.</li>
 *   <li><b>Null policy</b>: specific implementations may reject {@code null} elements.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * The default empty instance returned by {@link #empty()} is provided by {@link Empty#vector()},
 * which currently delegates to the project’s default persistent vector implementation.
 *
 * @param <E> the element type
 *
 * @see OSequence
 * @see Empty#vector()
 * @see ChunkedOVector
 */
public interface OVector<E> extends OSequence<E> {

    /**
     * Returns the canonical empty vector instance.
     *
     * <p>
     * This is a convenience factory that delegates to {@link Empty#vector()}.
     * The returned instance is immutable and typically shared.
     *
     * @param <E> the element type
     * @return an empty persistent vector
     */
    static <E> OVector<E> empty() {
        return Empty.vector();
    }
}
