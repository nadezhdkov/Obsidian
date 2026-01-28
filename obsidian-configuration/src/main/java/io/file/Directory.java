package io.file;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Directory {

    @Getter
    private final Path path;

    private Directory(String dirPath) {
        this.path = Paths.get(dirPath);
    }

    @Contract("_ -> new")
    public static @NotNull Directory at(@NotNull String dirPath) {
        return new Directory(Objects.requireNonNull(dirPath));
    }

    @Contract("_ -> new")
    public static @NotNull Directory at(@NotNull Path path) {
        return new Directory(Objects.requireNonNull(path).toString());
    }

    public Directory create() {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public boolean exists() {
        return Files.exists(path) && Files.isDirectory(path);
    }

    public boolean isEmpty() {
        if (!exists()) return true;
        try (var entries = Files.list(path)) {
            return entries.findAny().isEmpty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Path> list() {
        try (var stream = Files.list(path)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listNames() {
        try (var stream = Files.list(path)) {
            return stream
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Directory deleteRecursively() {
        if (Files.notExists(path)) return this;

        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(this::deletePathUnchecked);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Directory clean() {
        if (Files.notExists(path)) return this;

        try (var stream = Files.list(path)) {
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    Directory.at(p).deleteRecursively();
                } else {
                    deletePathUnchecked(p);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void deletePathUnchecked(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
