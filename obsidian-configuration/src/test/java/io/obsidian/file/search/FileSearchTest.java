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

package io.obsidian.file.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileSearch")
class FileSearchTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {
        testFile = tempDir.resolve("log.txt");
        Files.writeString(testFile, "INFO: Application started\nERROR: Connection failed\nINFO: Retry successful\nWARN: Low memory\nERROR: Timeout");
    }

    @Nested
    @DisplayName("filter()")
    class Filter {

        @Test
        @DisplayName("should return lines matching predicate")
        void filtersLines() {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            var errors = search.filter(line -> line.startsWith("ERROR"));
            assertEquals(2, errors.size());
            assertTrue(errors.get(0).contains("Connection failed"));
        }

        @Test
        @DisplayName("should return empty list when no match")
        void noMatches() {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            var results = search.filter(line -> line.startsWith("DEBUG"));
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("grep()")
    class Grep {

        @Test
        @DisplayName("should match regex patterns")
        void matchesRegex() {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            var results = search.grep("ERROR.*");
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("should support complex patterns")
        void complexRegex() {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            var results = search.grep("(INFO|WARN):");
            assertEquals(3, results.size());
        }
    }

    @Nested
    @DisplayName("replaceAll()")
    class ReplaceAll {

        @Test
        @DisplayName("should replace all regex matches in file")
        void replacesContent() throws Exception {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            search.replaceAll("ERROR", "FATAL");

            var content = Files.readString(testFile);
            assertFalse(content.contains("ERROR"));
            assertTrue(content.contains("FATAL"));
        }
    }

    @Nested
    @DisplayName("filterAndSave()")
    class FilterAndSave {

        @Test
        @DisplayName("should save filtered lines to a new file")
        void savesFiltered() throws Exception {
            var target = tempDir.resolve("errors.txt");
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            search.filterAndSave(line -> line.startsWith("ERROR"), target.toString());

            assertTrue(Files.exists(target));
            var lines = Files.readAllLines(target);
            assertEquals(2, lines.size());
        }
    }

    @Nested
    @DisplayName("Counting")
    class Counting {

        @Test
        @DisplayName("countLines should return total line count")
        void countsLines() {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            assertEquals(5, search.countLines());
        }

        @Test
        @DisplayName("count should count occurrences of a string")
        void countsOccurrences() {
            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            assertEquals(2, search.count("ERROR"));
            assertEquals(2, search.count("INFO"));
        }
    }

    @Nested
    @DisplayName("contentEquals()")
    class ContentEquals {

        @Test
        @DisplayName("should return true for identical files")
        void identicalFiles() throws Exception {
            var copy = tempDir.resolve("copy.txt");
            Files.copy(testFile, copy);

            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            assertTrue(search.contentEquals(copy.toString()));
        }

        @Test
        @DisplayName("should return false for different files")
        void differentFiles() throws Exception {
            var other = tempDir.resolve("other.txt");
            Files.writeString(other, "different content");

            var search = new FileSearch(testFile, StandardCharsets.UTF_8);
            assertFalse(search.contentEquals(other.toString()));
        }
    }
}
