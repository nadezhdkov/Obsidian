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

import io.obsidian.file.io.FileReader;
import io.obsidian.file.io.FileWriter;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Text search, filtering, and content transformation operations on a file.
 *
 * <p>Delegates reading to {@link FileReader} and writing to {@link FileWriter}
 * to maintain SRP separation.</p>
 *
 * @since 1.0
 */
public final class FileSearch {

    private final FileReader reader;
    private final FileWriter writer;

    public FileSearch(@NotNull Path path, @NotNull Charset charset) {
        this.reader = new FileReader(path, charset);
        this.writer = new FileWriter(path, charset);
    }

    /**
     * Filters lines by predicate.
     *
     * @param predicate the line filter
     * @return lines matching the predicate
     */
    public @NotNull List<String> filter(@NotNull Predicate<String> predicate) {
        try (var stream = reader.lines()) {
            return stream.filter(predicate).collect(Collectors.toList());
        }
    }

    /**
     * Filters lines and saves matching lines to a target file.
     *
     * @param predicate  the line filter
     * @param targetPath the destination file path
     */
    public void filterAndSave(@NotNull Predicate<String> predicate, @NotNull String targetPath) {
        var filtered = filter(predicate);
        new FileWriter(Path.of(targetPath), java.nio.charset.StandardCharsets.UTF_8).writeLines(filtered);
    }

    /**
     * Returns lines matching the given regex pattern.
     *
     * @param regex the regular expression
     * @return lines where the regex finds a match
     */
    public @NotNull List<String> grep(@NotNull String regex) {
        var pattern = Pattern.compile(regex);
        return filter(line -> pattern.matcher(line).find());
    }

    /**
     * Replaces all occurrences of a regex in the file content and saves.
     *
     * @param regex       the regular expression to match
     * @param replacement the replacement string
     */
    public void replaceAll(@NotNull String regex, @NotNull String replacement) {
        var content  = reader.readAllText();
        var replaced = content.replaceAll(regex, replacement);
        writer.write(replaced);
    }

    /**
     * Processes each line of the file with the given consumer.
     *
     * @param processor the line consumer
     */
    public void processLines(@NotNull Consumer<String> processor) {
        try (var stream = reader.lines()) {
            stream.forEach(processor);
        }
    }

    /**
     * Counts the total number of lines in the file.
     *
     * @return line count
     */
    public long countLines() {
        try (var stream = reader.lines()) {
            return stream.count();
        }
    }

    /**
     * Counts the occurrences of a search string in the file.
     *
     * @param searchString the string to search for
     * @return total number of occurrences across all lines
     */
    public long count(@NotNull String searchString) {
        try (var stream = reader.lines()) {
            return stream
                    .mapToLong(line -> {
                        int len = searchString.length();
                        return (line.length() - line.replace(searchString, "").length()) / len;
                    })
                    .sum();
        }
    }

    /**
     * Checks whether two files have identical byte content.
     *
     * @param otherPath the path of the other file
     * @return {@code true} if byte-level content is equal
     */
    public boolean contentEquals(@NotNull String otherPath) {
        try {
            byte[] content1 = reader.readAllBytes();
            byte[] content2 = new FileReader(Path.of(otherPath), java.nio.charset.StandardCharsets.UTF_8).readAllBytes();
            return Arrays.equals(content1, content2);
        } catch (Exception e) {
            return false;
        }
    }
}
