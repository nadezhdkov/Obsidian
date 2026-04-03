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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Directory")
class DirectoryTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create directory and parents")
        void createsDeepDir() {
            var deep = tempDir.resolve("a").resolve("b").resolve("c");
            Directory.at(deep.toString()).create();
            assertTrue(Files.isDirectory(deep));
        }

        @Test
        @DisplayName("should be idempotent")
        void idempotent() {
            var dir = tempDir.resolve("exists");
            Directory.at(dir.toString()).create();
            assertDoesNotThrow(() -> Directory.at(dir.toString()).create());
        }
    }

    @Nested
    @DisplayName("exists()")
    class Exists {

        @Test
        @DisplayName("should return true for existing directory")
        void existingDir() {
            assertTrue(Directory.at(tempDir.toString()).exists());
        }

        @Test
        @DisplayName("should return false for non-existent path")
        void nonExistent() {
            assertFalse(Directory.at(tempDir.resolve("nope").toString()).exists());
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmpty {

        @Test
        @DisplayName("should return true for empty directory")
        void emptyDir() {
            var empty = tempDir.resolve("empty");
            Directory.at(empty.toString()).create();
            assertTrue(Directory.at(empty.toString()).isEmpty());
        }

        @Test
        @DisplayName("should return false for non-empty directory")
        void nonEmptyDir() throws Exception {
            Files.writeString(tempDir.resolve("file.txt"), "content");
            assertFalse(Directory.at(tempDir.toString()).isEmpty());
        }
    }

    @Nested
    @DisplayName("list() and listNames()")
    class Listing {

        @Test
        @DisplayName("list should return child paths")
        void listsPaths() throws Exception {
            Files.writeString(tempDir.resolve("a.txt"), "a");
            Files.writeString(tempDir.resolve("b.txt"), "b");

            List<Path> children = Directory.at(tempDir.toString()).list();
            assertEquals(2, children.size());
        }

        @Test
        @DisplayName("listNames should return child names")
        void listsNames() throws Exception {
            Files.writeString(tempDir.resolve("alpha.txt"), "a");
            Files.createDirectories(tempDir.resolve("subdir"));

            List<String> names = Directory.at(tempDir.toString()).listNames();
            assertEquals(2, names.size());
            assertTrue(names.contains("alpha.txt"));
            assertTrue(names.contains("subdir"));
        }
    }

    @Nested
    @DisplayName("deleteRecursively()")
    class DeleteRecursively {

        @Test
        @DisplayName("should remove directory and all contents")
        void deletesAll() throws Exception {
            var dir = tempDir.resolve("deleteme");
            Files.createDirectories(dir.resolve("sub"));
            Files.writeString(dir.resolve("file.txt"), "data");
            Files.writeString(dir.resolve("sub").resolve("nested.txt"), "nested");

            Directory.at(dir.toString()).deleteRecursively();
            assertFalse(Files.exists(dir));
        }

        @Test
        @DisplayName("should be safe on non-existent directory")
        void safeOnMissing() {
            var missing = tempDir.resolve("ghost");
            assertDoesNotThrow(() -> Directory.at(missing.toString()).deleteRecursively());
        }
    }

    @Nested
    @DisplayName("clean()")
    class Clean {

        @Test
        @DisplayName("should remove all contents but keep the directory")
        void keepsDirRemovesContents() throws Exception {
            var dir = tempDir.resolve("cleanme");
            Files.createDirectories(dir.resolve("sub"));
            Files.writeString(dir.resolve("a.txt"), "a");
            Files.writeString(dir.resolve("sub").resolve("b.txt"), "b");

            Directory.at(dir.toString()).clean();

            assertTrue(Files.isDirectory(dir));
            assertTrue(Directory.at(dir.toString()).isEmpty());
        }
    }
}
