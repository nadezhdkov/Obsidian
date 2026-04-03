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

package io.obsidian.file.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileOperations")
class FileOperationsTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "content");
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("createIfNotExists should create file and parents")
        void createsFileAndParents() {
            var nested = tempDir.resolve("a").resolve("b").resolve("new.txt");
            var ops = new FileOperations(nested);
            ops.createIfNotExists();
            assertTrue(Files.exists(nested));
        }

        @Test
        @DisplayName("createIfNotExists should be idempotent")
        void idempotentCreate() {
            var ops = new FileOperations(testFile);
            assertDoesNotThrow(ops::createIfNotExists);
            assertTrue(Files.exists(testFile));
        }

        @Test
        @DisplayName("create should create directories and file")
        void fullCreate() {
            var deep = tempDir.resolve("x").resolve("y").resolve("z.txt");
            var ops = new FileOperations(deep);
            ops.create();
            assertTrue(Files.exists(deep));
        }

        @Test
        @DisplayName("delete should remove existing file")
        void deletesFile() {
            var ops = new FileOperations(testFile);
            ops.delete();
            assertFalse(Files.exists(testFile));
        }

        @Test
        @DisplayName("delete should not throw for non-existent file")
        void deleteNonExistent() {
            var ops = new FileOperations(tempDir.resolve("nope.txt"));
            assertDoesNotThrow(ops::delete);
        }

        @Test
        @DisplayName("deleteIf should only delete when predicate matches")
        void conditionalDelete() {
            var ops = new FileOperations(testFile);
            ops.deleteIf(p -> false);
            assertTrue(Files.exists(testFile));

            ops.deleteIf(p -> true);
            assertFalse(Files.exists(testFile));
        }
    }

    @Nested
    @DisplayName("Manipulation")
    class Manipulation {

        @Test
        @DisplayName("copyTo should copy file content")
        void copiesFile() throws Exception {
            var target = tempDir.resolve("copy.txt");
            var ops = new FileOperations(testFile);
            ops.copyTo(target.toString());

            assertTrue(Files.exists(target));
            assertEquals("content", Files.readString(target));
        }

        @Test
        @DisplayName("copyToIfNotExists should skip when target exists")
        void skipsCopyWhenExists() throws Exception {
            var target = tempDir.resolve("existing.txt");
            Files.writeString(target, "original");

            var ops = new FileOperations(testFile);
            ops.copyToIfNotExists(target.toString());
            assertEquals("original", Files.readString(target));
        }

        @Test
        @DisplayName("moveTo should move file and remove source")
        void movesFile() throws Exception {
            var target = tempDir.resolve("moved.txt");
            var ops = new FileOperations(testFile);
            ops.moveTo(target.toString());

            assertTrue(Files.exists(target));
            assertFalse(Files.exists(testFile));
            assertEquals("content", Files.readString(target));
        }

        @Test
        @DisplayName("renameTo should rename within parent directory")
        void renamesFile() throws Exception {
            var ops = new FileOperations(testFile);
            Path newPath = ops.renameTo("renamed.txt");

            assertTrue(Files.exists(newPath));
            assertFalse(Files.exists(testFile));
            assertEquals("content", Files.readString(newPath));
        }

        @Test
        @DisplayName("backup should create timestamped .bak copy")
        void backsUpFile() throws Exception {
            var ops = new FileOperations(testFile);
            ops.backup();

            var backups = Files.list(tempDir)
                    .filter(p -> p.toString().endsWith(".bak"))
                    .toList();
            assertEquals(1, backups.size());
            assertEquals("content", Files.readString(backups.get(0)));
        }
    }

    @Nested
    @DisplayName("Static Utilities")
    class StaticUtilities {

        @Test
        @DisplayName("createTemp should create a temp file")
        void createsTemp() {
            var tempPath = FileOperations.createTemp("obsidian-", ".tmp");
            assertTrue(Files.exists(tempPath));
        }

        @Test
        @DisplayName("isSameFile should detect identical paths")
        void detectsSameFile() {
            assertTrue(FileOperations.isSameFile(testFile.toString(), testFile.toString()));
        }

        @Test
        @DisplayName("isSameFile should return false for different files")
        void detectsDifferentFiles() throws Exception {
            var other = tempDir.resolve("other.txt");
            Files.writeString(other, "other");
            assertFalse(FileOperations.isSameFile(testFile.toString(), other.toString()));
        }
    }
}
