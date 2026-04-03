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

package io.obsidian.file;

import io.obsidian.file.attribute.FileMetadata;
import io.obsidian.file.hash.Sha256Hash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileHandle — Integration")
class FileHandleTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("End-to-End Fluent Chain")
    class FluentChain {

        @Test
        @DisplayName("create → write → read roundtrip")
        void createWriteRead() {
            var file = tempDir.resolve("chain.txt");

            var content = FileHandle.at(file)
                    .create()
                    .write("Obsidian")
                    .readAllText();

            assertEquals("Obsidian", content);
        }

        @Test
        @DisplayName("write → append → readAllLines")
        void writeAppendRead() {
            var file = tempDir.resolve("append.txt");

            var lines = FileHandle.at(file)
                    .create()
                    .write("Line 1\n")
                    .append("Line 2\n")
                    .append("Line 3")
                    .readAllLines();

            assertEquals(3, lines.size());
            assertEquals("Line 1", lines.get(0));
        }

        @Test
        @DisplayName("writeLines → filter")
        void writeLinesFilter() {
            var file = tempDir.resolve("filter.txt");
            var handle = FileHandle.at(file).create();

            handle.writeLines(List.of("apple", "banana", "avocado", "blueberry"));
            var aFruits = handle.filter(line -> line.startsWith("a"));

            assertEquals(2, aFruits.size());
            assertTrue(aFruits.contains("apple"));
            assertTrue(aFruits.contains("avocado"));
        }

        @Test
        @DisplayName("write → compress → decompress roundtrip")
        void compressionChain() {
            var source = tempDir.resolve("src.txt");
            var compressed = tempDir.resolve("src.txt.gz");
            var restored = tempDir.resolve("restored.txt");

            FileHandle.at(source)
                    .create()
                    .write("Compression test content")
                    .compress(compressed.toString());

            FileHandle.at(compressed)
                    .decompress(restored.toString());

            assertEquals("Compression test content", FileHandle.at(restored).readAllText());
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("metadata() should return atomic snapshot")
        void atomicMetadata() throws Exception {
            var file = tempDir.resolve("meta.txt");
            Files.writeString(file, "Metadata test");

            FileMetadata meta = FileHandle.at(file).metadata();

            assertEquals("meta.txt", meta.fileName());
            assertEquals("txt", meta.extension());
            assertEquals(13, meta.sizeBytes());
            assertEquals("13 B", meta.sizeFormatted());
            assertTrue(meta.isRegularFile());
            assertFalse(meta.isDirectory());
        }
    }

    @Nested
    @DisplayName("Hashing")
    class Hashing {

        @Test
        @DisplayName("hash with Strategy should produce correct length")
        void hashWithStrategy() throws Exception {
            var file = tempDir.resolve("hash.txt");
            Files.writeString(file, "Hash test");

            var hash = FileHandle.at(file).hash(new Sha256Hash());
            assertEquals(64, hash.length());
        }

        @Test
        @DisplayName("hash with algorithm name should match Strategy result")
        void hashConsistency() throws Exception {
            var file = tempDir.resolve("hash2.txt");
            Files.writeString(file, "Consistency");

            var handle = FileHandle.at(file);
            assertEquals(
                    handle.hash(new Sha256Hash()),
                    handle.hash("SHA-256")
            );
        }
    }

    @Nested
    @DisplayName("File Operations via Handle")
    class Operations {

        @Test
        @DisplayName("copyTo should preserve content")
        void copyPreservesContent() {
            var source = tempDir.resolve("original.txt");
            var target = tempDir.resolve("copied.txt");

            FileHandle.at(source).create().write("Copy me");
            FileHandle.at(source).copyTo(target.toString());

            assertTrue(Files.exists(target));
            assertEquals("Copy me", FileHandle.at(target).readAllText());
        }

        @Test
        @DisplayName("backup should create .bak file")
        void backupCreatesFile() throws Exception {
            var file = tempDir.resolve("backup.txt");
            FileHandle.at(file).create().write("Backup data").backup();

            var baks = Files.list(tempDir)
                    .filter(p -> p.toString().endsWith(".bak"))
                    .toList();

            assertEquals(1, baks.size());
        }

        @Test
        @DisplayName("delete should remove file")
        void deletesFile() {
            var file = tempDir.resolve("deleteme.txt");
            FileHandle.at(file).create().write("bye");
            assertTrue(Files.exists(file));

            FileHandle.at(file).delete();
            assertFalse(Files.exists(file));
        }
    }

    @Nested
    @DisplayName("Attributes via Handle")
    class Attributes {

        @Test
        @DisplayName("exists should reflect filesystem state")
        void existsCheck() {
            var file = tempDir.resolve("exists.txt");
            assertFalse(FileHandle.at(file).exists());

            FileHandle.at(file).create();
            assertTrue(FileHandle.at(file).exists());
        }

        @Test
        @DisplayName("getFileName and getExtension should work correctly")
        void nameAndExtension() {
            var file = tempDir.resolve("doc.pdf");
            var handle = FileHandle.at(file);
            assertEquals("doc.pdf", handle.getFileName());
            assertEquals("pdf", handle.getExtension());
            assertEquals("doc", handle.getFileNameWithoutExtension());
        }

        @Test
        @DisplayName("size should return correct byte count")
        void sizeCheck() throws Exception {
            var file = tempDir.resolve("sized.txt");
            Files.writeString(file, "12345");
            assertEquals(5, FileHandle.at(file).size());
        }
    }

    @Nested
    @DisplayName("Static Utilities")
    class StaticUtils {

        @Test
        @DisplayName("createTemp should create a temp file")
        void createTemp() {
            var handle = FileHandle.createTemp("obsidian-test-", ".tmp");
            assertTrue(handle.exists());
        }

        @Test
        @DisplayName("getExtension static should extract extension")
        void staticExtension() {
            assertEquals("java", FileHandle.getExtension("Main.java"));
            assertEquals("", FileHandle.getExtension("Makefile"));
        }
    }
}
