package io.dotenv.core.util;

import io.dotenv.core.DotenvEntry;
import io.dotenv.core.exception.DotenvException;
import io.dotenv.core.util.internal.DotenvLineMatcher;
import io.dotenv.core.util.internal.DotenvQuote;
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
