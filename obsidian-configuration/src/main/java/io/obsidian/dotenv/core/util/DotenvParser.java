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

package io.obsidian.dotenv.core.util;

import io.obsidian.dotenv.core.DotenvEntry;
import io.obsidian.dotenv.core.exception.DotenvException;
import io.obsidian.dotenv.core.util.internal.DotenvLineMatcher;
import io.obsidian.dotenv.core.util.internal.DotenvQuote;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DotenvParser {

    private static final Pattern WHITE_SPACE = Pattern.compile("^\\s*$");

    private final DotenvReader      reader;
    private final DotenvLineMatcher matcher;
    private final boolean           throwIfMissing;
    private final boolean           throwIfMalformed;

    public DotenvParser(DotenvReader reader, boolean throwIfMissing, boolean throwIfMalformed) {
        this.reader           = reader;
        this.throwIfMissing   = throwIfMissing;
        this.throwIfMalformed = throwIfMalformed;
        this.matcher          = new DotenvLineMatcher();
    }

    public List<DotenvEntry> parse() {
        var lines   = readLinesSafe();
        var entries = new ArrayList<DotenvEntry>();
        var buffer  = new StringBuilder();

        for (String line : lines) {
            processLine(line, buffer, entries);
        }

        return entries;
    }

    private void processLine(String line, StringBuilder buffer, List<DotenvEntry> entries) {
        if (shouldSkip(line, buffer)) return;

        buffer.append(line);
        var currentText = buffer.toString();
        var match       = matcher.match(currentText);

        if (match.isEmpty()) {
            handleError("Malformed entry: " + currentText);
            buffer.setLength(0);
            return;
        }

        var entry = match.get();

        if (DotenvQuote.isMultilineStart(entry.value())) {
            buffer.append("\n");
            return;
        }

        if (DotenvQuote.hasUnbalancedQuotes(entry.value())) {
            handleError("Malformed entry, unmatched quotes: " + currentText);
            buffer.setLength(0);
            return;
        }

        entries.add(finalizeEntry(entry));
        buffer.setLength(0);
    }

    private @NotNull DotenvEntry finalizeEntry(@NotNull DotenvEntry entry) {
        var cleanValue = DotenvQuote.stripQuotes(entry.value());
        return new DotenvEntry(entry.key(), cleanValue);
    }

    private boolean shouldSkip(String line, @NotNull StringBuilder buffer) {
        return buffer.isEmpty() && (isBlank(line) || isComment(line));
    }

    private List<String> readLinesSafe() {
        try {
            return reader.read();
        } catch (DotenvException e) {
            if (throwIfMissing) throw new RuntimeException("Failed to read .env file", e);
            return List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read .env file", e);
        }
    }

    private void handleError(String msg) {
        if (throwIfMalformed) throw new DotenvException(msg);
    }

    private static boolean isBlank(String s) {
        return s == null || WHITE_SPACE.matcher(s).matches();
    }

    private static boolean isComment(String s) {
        return s.startsWith("#") || s.startsWith("//");
    }
}
