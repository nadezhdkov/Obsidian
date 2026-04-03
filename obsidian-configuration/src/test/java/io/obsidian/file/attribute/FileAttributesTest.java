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

package io.obsidian.file.attribute;

import io.obsidian.file.exception.FileReadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileAttributes")
class FileAttributesTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("sample.txt");
        Files.writeString(testFile, "Hello World");
    }

    @Nested
    @DisplayName("Basic Queries")
    class BasicQueries {

        @Test
        @DisplayName("exists should return true for existing file")
        void existingFile() {
            assertTrue(new FileAttributes(testFile).exists());
        }

        @Test
        @DisplayName("exists should return false for non-existent file")
        void nonExistent() {
            assertFalse(new FileAttributes(tempDir.resolve("ghost.txt")).exists());
        }

        @Test
        @DisplayName("size should return correct byte count")
        void correctSize() {
            assertEquals(11, new FileAttributes(testFile).size());
        }

        @Test
        @DisplayName("sizeFormatted should format bytes")
        void formattedSize() {
            var formatted = new FileAttributes(testFile).sizeFormatted();
            assertEquals("11 B", formatted);
        }

        @Test
        @DisplayName("isRegularFile should return true for files")
        void isRegular() {
            assertTrue(new FileAttributes(testFile).isRegularFile());
        }

        @Test
        @DisplayName("isDirectory should return true for directories")
        void isDir() {
            assertTrue(new FileAttributes(tempDir).isDirectory());
        }
    }

    @Nested
    @DisplayName("Name Operations")
    class NameOperations {

        @Test
        @DisplayName("getFileName should return the file name")
        void fileName() {
            assertEquals("sample.txt", new FileAttributes(testFile).getFileName());
        }

        @Test
        @DisplayName("getFileNameWithoutExtension should strip extension")
        void nameWithoutExt() {
            assertEquals("sample", new FileAttributes(testFile).getFileNameWithoutExtension());
        }

        @Test
        @DisplayName("getExtension should return the extension")
        void extension() {
            assertEquals("txt", new FileAttributes(testFile).getExtension());
        }

        @Test
        @DisplayName("getExtension should return empty for no-extension files")
        void noExtension() throws Exception {
            var noExt = tempDir.resolve("Makefile");
            Files.createFile(noExt);
            assertEquals("", new FileAttributes(noExt).getExtension());
        }
    }

    @Nested
    @DisplayName("Timestamps")
    class Timestamps {

        @Test
        @DisplayName("getLastModifiedTime should return a recent instant")
        void lastModified() {
            var lmt = new FileAttributes(testFile).getLastModifiedTime();
            assertNotNull(lmt);
            assertTrue(lmt.isBefore(Instant.now().plusSeconds(1)));
        }

        @Test
        @DisplayName("getCreationTime should return a valid instant")
        void creationTime() {
            var ct = new FileAttributes(testFile).getCreationTime();
            assertNotNull(ct);
        }

        @Test
        @DisplayName("setLastModifiedTime should update the timestamp")
        void setModifiedTime() {
            var attrs = new FileAttributes(testFile);
            var past = Instant.parse("2020-01-01T00:00:00Z");
            attrs.setLastModifiedTime(past);
            assertEquals(past, attrs.getLastModifiedTime());
        }
    }

    @Nested
    @DisplayName("Snapshot")
    class Snapshot {

        @Test
        @DisplayName("snapshot should return a consistent FileMetadata")
        void atomicSnapshot() {
            var meta = new FileAttributes(testFile).snapshot();
            assertNotNull(meta);
            assertEquals("sample.txt", meta.fileName());
            assertEquals("txt", meta.extension());
            assertEquals(11, meta.sizeBytes());
            assertTrue(meta.isRegularFile());
            assertFalse(meta.isDirectory());
            assertFalse(meta.isSymbolicLink());
        }

        @Test
        @DisplayName("snapshot should throw FileReadException for missing file")
        void snapshotMissing() {
            var attrs = new FileAttributes(tempDir.resolve("missing.txt"));
            assertThrows(FileReadException.class, attrs::snapshot);
        }
    }

    @Nested
    @DisplayName("Static Utilities")
    class StaticUtilities {

        @Test
        @DisplayName("extractExtension should extract from filename strings")
        void extractExt() {
            assertEquals("yml", FileAttributes.extractExtension("config.yml"));
            assertEquals("gz", FileAttributes.extractExtension("archive.tar.gz"));
            assertEquals("", FileAttributes.extractExtension("Makefile"));
            assertEquals("", FileAttributes.extractExtension(".gitignore"));
        }
    }
}
