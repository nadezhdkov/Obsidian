package io.dotenv.core;

import io.dotenv.core.exception.DotenvException;
import io.dotenv.core.util.DotenvParser;
import io.dotenv.core.util.DotenvReader;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DotenvBuilder {

    private String  filename         = ".env";
    private String  directory        = "./";
    private boolean systemProperties = false;
    private boolean throwIfMissing   = false;
    private boolean throwIfMalformed = true;

    public DotenvBuilder() {}

    public DotenvBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }

    public DotenvBuilder directory(String directory) {
        this.directory = directory;
        return this;
    }

    public DotenvBuilder systemProperties() {
        this.systemProperties = true;
        return this;
    }

    public DotenvBuilder throwIfMissing() {
        this.throwIfMissing = true;
        return this;
    }

    public DotenvBuilder throwIfMalformed() {
        this.throwIfMalformed = true;
        return this;
    }

    public DotenvBuilder ignoreIfMissing() {
        this.throwIfMissing = false;
        return this;
    }

    public DotenvBuilder ignoreIfMalformed() {
        this.throwIfMalformed = false;
        return this;
    }

    public Dotenv load() throws DotenvException {
        var reader  = new DotenvReader(directory, filename);
        var parser  = new DotenvParser(reader, throwIfMissing, throwIfMalformed);
        var entries = parser.parse();

        if (systemProperties) {
            entries.forEach(e -> System.setProperty(e.key(), e.value()));
        }

        return new DotenvImpl(entries);
    }

    static class DotenvImpl implements Dotenv {

        private final Map<String, String> envVars;
        private final Set<DotenvEntry>    fileEntries;
        private final Set<DotenvEntry>    allEntries;

        DotenvImpl(List<DotenvEntry> entriesFromFile) {
            var fileMap   = toMap(entriesFromFile);
            var systemMap = System.getenv();
            var mergedMap = new HashMap<>(fileMap);

            mergedMap.putAll(systemMap);

            this.envVars     = Collections.unmodifiableMap(mergedMap);
            this.fileEntries = toEntrySet(fileMap);
            this.allEntries  = toEntrySet(mergedMap);
        }

        @Override
        public Set<DotenvEntry> entries() {
            return allEntries;
        }

        @Override
        public Set<DotenvEntry> entries(Dotenv.Filter filter) {
            return filter == null ? allEntries : fileEntries;
        }

        @Override
        public String get(String key) {
            return envVars.get(key);
        }

        @Override
        public String get(String key, String defaultValue) {
            return envVars.getOrDefault(key, defaultValue);
        }

        private static Map<String, String> toMap(@NotNull List<DotenvEntry> entries) {
            return entries.stream()
                    .collect(Collectors.toMap(DotenvEntry::key, DotenvEntry::value, (a, b) -> b));
        }

        private static Set<DotenvEntry> toEntrySet(@NotNull Map<String, String> map) {
            return map.entrySet().stream()
                    .map(e -> new DotenvEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }
}
