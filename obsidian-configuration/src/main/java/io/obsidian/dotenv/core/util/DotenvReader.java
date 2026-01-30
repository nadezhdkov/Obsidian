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
