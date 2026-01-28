package io.dotenv.core.util;

import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DotenvReader {

    private final String directory;
    private final String filename;

    public DotenvReader(String directory, String filename) {
        this.directory = directory;
        this.filename  = filename;
    }

    public List<String> read() throws IOException {
        var location = resolveLocation();
        var path     = toPath(location);

        if (Files.exists(path)) {
            return Files.readAllLines(path);
        }

        return loadFromClasspath(location);
    }

    private @NotNull String resolveLocation() {
        var cleanDir = directory
                .replaceAll("\\\\", "/")
                .replaceAll("\\.env$", "")
                .replaceFirst("/$", "")
                + "/";

        return cleanDir + filename;
    }

    private @NotNull Path toPath(String location) {
        if (isFileSystemUri(location)) {
            return Paths.get(URI.create(location));
        }
        return Paths.get(location);
    }

    private @NotNull @Unmodifiable List<String> loadFromClasspath(@NotNull String location) {
        var cleanLoc = location.replaceFirst("^\\./", "/");
        return ClasspathResourceLoader.loadFileFromClasspath(cleanLoc).toList();
    }

    private boolean isFileSystemUri(@NotNull String loc) {
        return loc.startsWith("file:")
                || loc.startsWith("jimfs:")
                || loc.startsWith("android.resource:");
    }

}
