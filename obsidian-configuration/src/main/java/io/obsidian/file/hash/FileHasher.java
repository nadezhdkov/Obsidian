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

import io.obsidian.file.exception.FileHashException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Context class for the hashing Strategy pattern.
 *
 * <p>Reads a file's bytes and delegates hashing to a {@link HashAlgorithm}
 * implementation, or accepts an algorithm name for convenience.</p>
 *
 * <h3>Usage with Strategy</h3>
 * <pre>{@code
 * String hash = FileHasher.hash(Path.of("file.txt"), new Sha256Hash());
 * }</pre>
 *
 * <h3>Usage with algorithm name</h3>
 * <pre>{@code
 * String hash = FileHasher.hash(Path.of("file.txt"), "SHA-256");
 * }</pre>
 *
 * @since 1.0
 */
public final class FileHasher {

    private FileHasher() {
        // utility class
    }

    /**
     * Computes the hash of a file using the given {@link HashAlgorithm} strategy.
     *
     * @param path      the file to hash
     * @param algorithm the hashing strategy
     * @return hex-encoded hash string
     * @throws FileHashException if reading or hashing fails
     */
    public static @NotNull String hash(@NotNull Path path, @NotNull HashAlgorithm algorithm) {
        try {
            byte[] data = Files.readAllBytes(path);
            return algorithm.hash(data);
        } catch (IOException e) {
            throw new FileHashException(path, algorithm.algorithmName(), e);
        }
    }

    /**
     * Convenience method: computes the hash using a JCA algorithm name.
     *
     * @param path          the file to hash
     * @param algorithmName the JCA algorithm name (e.g. {@code "SHA-256"}, {@code "MD5"})
     * @return hex-encoded hash string
     * @throws FileHashException if reading, hashing, or algorithm lookup fails
     */
    public static @NotNull String hash(@NotNull Path path, @NotNull String algorithmName) {
        try {
            var data   = Files.readAllBytes(path);
            var digest = MessageDigest.getInstance(algorithmName);
            var hash   = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new FileHashException(path, algorithmName, e);
        }
    }
}
