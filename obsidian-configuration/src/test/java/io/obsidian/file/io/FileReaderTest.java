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

package io.obsidian.file.io;

import io.obsidian.file.exception.FileReadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileReader")
class FileReaderTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Line 1\nLine 2\nLine 3\nLine 4\nLine 5");
    }

    @Nested
    @DisplayName("readAllText()")
    class ReadAllText {

        @Test
        @DisplayName("should read entire file content as a single string")
        void readsEntireContent() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            var content = reader.readAllText();
            assertEquals("Line 1\nLine 2\nLine 3\nLine 4\nLine 5", content);
        }

        @Test
        @DisplayName("should throw FileReadException for non-existent file")
        void throwsOnNonExistent() {
            var reader = new FileReader(tempDir.resolve("ghost.txt"), StandardCharsets.UTF_8);
            assertThrows(FileReadException.class, reader::readAllText);
        }
    }

    @Nested
    @DisplayName("readAllBytes()")
    class ReadAllBytes {

        @Test
        @DisplayName("should read raw bytes correctly")
        void readsBytes() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            byte[] bytes = reader.readAllBytes();
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        }
    }

    @Nested
    @DisplayName("readAllLines()")
    class ReadAllLines {

        @Test
        @DisplayName("should return all lines as a list")
        void readsLines() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            List<String> lines = reader.readAllLines();
            assertEquals(5, lines.size());
            assertEquals("Line 1", lines.get(0));
            assertEquals("Line 5", lines.get(4));
        }
    }

    @Nested
    @DisplayName("lines()")
    class Lines {

        @Test
        @DisplayName("should return a lazy stream of lines")
        void streamsLines() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            try (var stream = reader.lines()) {
                long count = stream.count();
                assertEquals(5, count);
            }
        }
    }

    @Nested
    @DisplayName("readFirstLines()")
    class ReadFirstLines {

        @Test
        @DisplayName("should return the first N lines")
        void readsFirstN() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            var first2 = reader.readFirstLines(2);
            assertEquals(2, first2.size());
            assertEquals("Line 1", first2.get(0));
            assertEquals("Line 2", first2.get(1));
        }

        @Test
        @DisplayName("should return all lines if N exceeds total")
        void readsAllIfNExceeds() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            var all = reader.readFirstLines(100);
            assertEquals(5, all.size());
        }
    }

    @Nested
    @DisplayName("readLastLines()")
    class ReadLastLines {

        @Test
        @DisplayName("should return the last N lines")
        void readsLastN() {
            var reader = new FileReader(testFile, StandardCharsets.UTF_8);
            var last2 = reader.readLastLines(2);
            assertEquals(2, last2.size());
            assertEquals("Line 4", last2.get(0));
            assertEquals("Line 5", last2.get(1));
        }
    }

    @Nested
    @DisplayName("readObject()")
    class ReadObject {

        @Test
        @DisplayName("should deserialize a Java object")
        void deserializesObject() throws Exception {
            var objFile = tempDir.resolve("obj.ser");
            var data = "Hello Obsidian";

            try (var os = Files.newOutputStream(objFile);
                 var oos = new ObjectOutputStream(os)) {
                oos.writeObject(data);
            }

            var reader = new FileReader(objFile, StandardCharsets.UTF_8);
            String result = reader.readObject(String.class);
            assertEquals("Hello Obsidian", result);
        }
    }
}
