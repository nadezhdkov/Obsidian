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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileHasher & HashAlgorithm Strategy")
class FileHasherTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("hashme.txt");
        Files.writeString(testFile, "Hello, Obsidian!");
    }

    @Nested
    @DisplayName("HashAlgorithm Implementations")
    class AlgorithmImplementations {

        @Test
        @DisplayName("Md5Hash should compute correct MD5")
        void md5Strategy() throws Exception {
            byte[] data = "Hello, Obsidian!".getBytes();
            var expected = HexFormat.of().formatHex(
                    MessageDigest.getInstance("MD5").digest(data)
            );

            var hash = new Md5Hash().hash(data);
            assertEquals(expected, hash);
            assertEquals("MD5", new Md5Hash().algorithmName());
        }

        @Test
        @DisplayName("Sha256Hash should compute correct SHA-256")
        void sha256Strategy() throws Exception {
            byte[] data = "Hello, Obsidian!".getBytes();
            var expected = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(data)
            );

            var hash = new Sha256Hash().hash(data);
            assertEquals(expected, hash);
            assertEquals("SHA-256", new Sha256Hash().algorithmName());
        }

        @Test
        @DisplayName("Different algorithms should produce different hashes")
        void differentHashes() {
            byte[] data = "test data".getBytes();
            assertNotEquals(new Md5Hash().hash(data), new Sha256Hash().hash(data));
        }
    }

    @Nested
    @DisplayName("FileHasher with Strategy")
    class HasherWithStrategy {

        @Test
        @DisplayName("hash with Md5Hash strategy should work on files")
        void hashWithMd5() {
            var hash = FileHasher.hash(testFile, new Md5Hash());
            assertNotNull(hash);
            assertFalse(hash.isEmpty());
            assertEquals(32, hash.length()); // MD5 = 16 bytes = 32 hex chars
        }

        @Test
        @DisplayName("hash with Sha256Hash strategy should work on files")
        void hashWithSha256() {
            var hash = FileHasher.hash(testFile, new Sha256Hash());
            assertNotNull(hash);
            assertEquals(64, hash.length()); // SHA-256 = 32 bytes = 64 hex chars
        }
    }

    @Nested
    @DisplayName("FileHasher with algorithm name")
    class HasherWithName {

        @Test
        @DisplayName("hash with 'MD5' string should match strategy result")
        void md5ByName() {
            var byStrategy = FileHasher.hash(testFile, new Md5Hash());
            var byName = FileHasher.hash(testFile, "MD5");
            assertEquals(byStrategy, byName);
        }

        @Test
        @DisplayName("hash with 'SHA-256' string should match strategy result")
        void sha256ByName() {
            var byStrategy = FileHasher.hash(testFile, new Sha256Hash());
            var byName = FileHasher.hash(testFile, "SHA-256");
            assertEquals(byStrategy, byName);
        }

        @Test
        @DisplayName("should throw FileHashException for unsupported algorithm")
        void unsupportedAlgorithm() {
            assertThrows(FileHashException.class, () ->
                    FileHasher.hash(testFile, "BOGUS-ALGO")
            );
        }

        @Test
        @DisplayName("should throw FileHashException for non-existent file")
        void nonExistentFile() {
            assertThrows(FileHashException.class, () ->
                    FileHasher.hash(tempDir.resolve("nope.txt"), "SHA-256")
            );
        }
    }

    @Nested
    @DisplayName("Determinism")
    class Determinism {

        @Test
        @DisplayName("same content should always produce same hash")
        void deterministic() {
            var hash1 = FileHasher.hash(testFile, new Sha256Hash());
            var hash2 = FileHasher.hash(testFile, new Sha256Hash());
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("different content should produce different hashes")
        void differentContent() throws Exception {
            var otherFile = tempDir.resolve("other.txt");
            Files.writeString(otherFile, "Different content");

            var hash1 = FileHasher.hash(testFile, new Sha256Hash());
            var hash2 = FileHasher.hash(otherFile, new Sha256Hash());
            assertNotEquals(hash1, hash2);
        }
    }
}
