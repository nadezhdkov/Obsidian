package io.dotenv.core.util;

import io.dotenv.core.exception.DotenvException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

public final class ClasspathResourceLoader {

    static @NotNull Stream<String> loadFileFromClasspath(String location) {
        Objects.requireNonNull(location, "Location must not be null");

        InputStream input = getResourceAsStream(location);
        if (input == null) {
            throw new DotenvException("Could not find %s on the classpath", location);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            return reader.lines().toList().stream();
        } catch (IOException e) {
            throw new DotenvException("Failed to read file: %s", e, location);
        }
    }

    private static InputStream getResourceAsStream(String location) {
        InputStream inputStream = ClasspathResourceLoader.class.getResourceAsStream(location);
        if (inputStream == null) {
            inputStream = ClassLoader.getSystemResourceAsStream(location);
        }
        return inputStream;
    }
}
