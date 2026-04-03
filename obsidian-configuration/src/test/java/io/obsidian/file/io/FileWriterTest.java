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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileWriter")
class FileWriterTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("output.txt");
        Files.createFile(testFile);
    }

    @Nested
    @DisplayName("write()")
    class Write {

        @Test
        @DisplayName("should write content truncating existing data")
        void writeTruncates() throws Exception {
            var writer = new FileWriter(testFile, StandardCharsets.UTF_8);
            writer.write("First");
            writer.write("Second");
            assertEquals("Second", Files.readString(testFile));
        }

        @Test
        @DisplayName("should create file if it does not exist")
        void createsIfMissing() throws Exception {
            var newFile = tempDir.resolve("new.txt");
            var writer = new FileWriter(newFile, StandardCharsets.UTF_8);
            writer.write("Created!");
            assertTrue(Files.exists(newFile));
            assertEquals("Created!", Files.readString(newFile));
        }
    }

    @Nested
    @DisplayName("append()")
    class Append {

        @Test
        @DisplayName("should append content to existing file")
        void appendsContent() throws Exception {
            var writer = new FileWriter(testFile, StandardCharsets.UTF_8);
            writer.write("Hello");
            writer.append(" World");
            assertEquals("Hello World", Files.readString(testFile));
        }
    }

    @Nested
    @DisplayName("writeLines()")
    class WriteLines {

        @Test
        @DisplayName("should write a list of lines")
        void writesLines() throws Exception {
            var writer = new FileWriter(testFile, StandardCharsets.UTF_8);
            writer.writeLines(List.of("A", "B", "C"));

            var lines = Files.readAllLines(testFile);
            assertEquals(3, lines.size());
            assertEquals("A", lines.get(0));
            assertEquals("C", lines.get(2));
        }
    }

    @Nested
    @DisplayName("writeBytes()")
    class WriteBytes {

        @Test
        @DisplayName("should write raw bytes")
        void writesBytes() throws Exception {
            var writer = new FileWriter(testFile, StandardCharsets.UTF_8);
            byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
            writer.writeBytes(data);
            assertEquals("Hello", Files.readString(testFile));
        }
    }

    @Nested
    @DisplayName("writeObject()")
    class WriteObject {

        @Test
        @DisplayName("should serialize a Java object")
        void serializesObject() throws Exception {
            var objFile = tempDir.resolve("obj.ser");
            Files.createFile(objFile);

            var writer = new FileWriter(objFile, StandardCharsets.UTF_8);
            writer.writeObject("Obsidian");

            try (var is = Files.newInputStream(objFile);
                 var ois = new ObjectInputStream(is)) {
                assertEquals("Obsidian", ois.readObject());
            }
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("should empty file content")
        void clearsContent() throws Exception {
            Files.writeString(testFile, "Not empty");
            var writer = new FileWriter(testFile, StandardCharsets.UTF_8);
            writer.clear();
            assertEquals("", Files.readString(testFile));
        }
    }
}
