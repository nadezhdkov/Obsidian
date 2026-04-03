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

package io.obsidian.file.hash;

import org.jetbrains.annotations.NotNull;

/**
 * Strategy interface for file hashing algorithms.
 *
 * <p>This is a {@code sealed} interface with known implementations for
 * MD5 and SHA-256. Additional algorithms can be added by extending the
 * permit list.</p>
 *
 * <h3>Strategy Pattern</h3>
 * <p>Each implementation encapsulates the digest computation, allowing
 * the {@link FileHasher} context to remain algorithm-agnostic.</p>
 *
 * @since 1.0
 */
public sealed interface HashAlgorithm permits Md5Hash, Sha256Hash {

    /**
     * Returns the JCA algorithm name (e.g. {@code "MD5"}, {@code "SHA-256"}).
     *
     * @return the algorithm identifier
     */
    @NotNull String algorithmName();

    /**
     * Computes the hash of the given raw data.
     *
     * @param data the byte array to hash
     * @return the hex-encoded hash string
     */
    @NotNull String hash(byte @NotNull [] data);
}
