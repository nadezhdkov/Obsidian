package io.file;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface File {

    /**
     * Creates a {@link Files} object for the specified file path.
     *
     * @param filePath the path of the file for which the {@code FileUtil} object is to be created.
     *                 This must not be {@code null}.
     * @return a {@code FileUtil} instance representing the specified file.
     * @throws IllegalArgumentException if the given file path is invalid or cannot be resolved.
     */
    @Contract("_ -> new")
    static @NotNull Files at(String filePath) {
        return Files.at(filePath);
    }

    /**
     * Creates a {@link Directory} object for the specified directory path.
     *
     * @param dirPath the path of the directory for which the {@code Directory} object is to be created.
     *                This must not be {@code null}.
     * @return a {@code Directory} instance representing the specified directory.
     * @throws IllegalArgumentException if the given directory path is invalid or cannot be resolved.
     */
    @Contract("_ -> new")
    static @NotNull Directory directory(String dirPath) {
        return Directory.at(dirPath);
    }

    /**
     * Retrieves a {@link Path} object for the specified string representation of a file or directory path.
     *
     * @param path the string representation of the file or directory path.
     *             This must not be {@code null}.
     * @return a {@link Path} object representing the specified path.
     * @throws IllegalArgumentException if the specified path is invalid or cannot be resolved.
     */
    static @NotNull Path get(String path) {
        return Paths.get(path);
    }

    /**
     * Combines one or more path segments into a single normalized {@link Path}.
     *
     * @param first the initial path segment. This segment must not be {@code null}.
     * @param more additional path segments to combine with the first segment. These segments
     *             must not be {@code null}.
     * @return a {@link Path} instance representing the combined and normalized result of the given path segments.
     * @throws IllegalArgumentException if any of the path segments are invalid or cannot be resolved.
     * @throws NullPointerException if the {@code first} parameter or any of the {@code more} parameters are {@code null}.
     */
    static @NotNull Path combine(String first, String... more) {
        return Paths.get(first, more);
    }

    /**
     * Converts the provided {@link Path} into an absolute path.
     * If the given path is already absolute, it will be normalized and returned.
     * Otherwise, it will be converted into an absolute path relative to
     * the current working directory and then normalized.
     *
     * @param path the {@link Path} to be converted to an absolute path. This must not be {@code null}.
     * @return the absolute and normalized form of the given {@link Path}.
     * @throws NullPointerException if the {@code path} parameter is {@code null}.
     */
    static @NotNull Path toAbsolutePath(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /**
     * Generates a hash for the specified file using the provided hashing algorithm.
     * The hash is computed based on the file's content and returned as a hexadecimal string.
     *
     * @param filePath the path to the file for which the hash is to be generated.
     *                 This must not be {@code null} or invalid.
     * @param algorithm the name of the hashing algorithm to use (e.g., "MD5", "SHA-256").
     *                  This must not be {@code null} or unsupported.
     * @return a {@code String} representing the computed hash in hexadecimal format.
     * @throws IllegalArgumentException if the file path is invalid, unreadable, or if the
     *                                  specified algorithm is unsupported.
     */
    static String getFileHash(String filePath, String algorithm) {
        return Files.getFileHash(filePath, algorithm);
    }

    /**
     * Reads the content of a file specified by the given file path.
     *
     * @param filePath the path to the file to be read. This must not be {@code null}
     *                 or reference an invalid file path.
     * @return a {@code String} containing the entire content of the file.
     * @throws IllegalArgumentException if the file path is invalid or the file cannot be read.
     */
    static String read(String filePath) {
        return Files.at(filePath).readAllText();
    }

    /**
     * Reads the content of the specified file and returns it as a byte array.
     *
     * @param filePath the path to the file to be read. This must not be {@code null}
     *                 or reference an invalid file path.
     * @return a byte array containing the content of the specified file.
     * @throws IllegalArgumentException if the file path is invalid or the file cannot be read.
     */
    static byte[] readBytes(String filePath) {
        return Files.at(filePath).readAllBytes();
    }

    /**
     * Writes the specified content to the file at the given file path.
     * If the file does not exist, it will be created. If it already exists,
     * its contents will be overwritten with the specified content.
     *
     * @param filePath the path to the file where the content is to be written.
     *                 This must not be {@code null} or reference an invalid file path.
     * @param content the content to be written to the file. This must not be {@code null}.
     * @throws IllegalArgumentException if the file path is invalid or cannot be resolved.
     */
    static void write(String filePath, String content) {
        Files.at(filePath).write(content);
    }

    /**
     * Writes the specified content to the file at the given file path.
     * If the file does not exist, it will be created. If the file already exists, the behavior is determined
     * by the {@code append} parameter.
     *
     * @param filePath the path to the file where the content is to be written. This must not be {@code null}
     *                 or reference an invalid file path.
     * @param content the content to be written to the file. This must not be {@code null}.
     * @param append a boolean flag indicating whether the content should be appended to the file
     *               if it already exists. If {@code true}, the content is appended to the end of the file;
     *               if {@code false}, the existing file content is overwritten.
     * @throws IllegalArgumentException if the file path is invalid or cannot be resolved.
     */
    static void write(String filePath, String content, boolean append) {
        Files.at(filePath).write(content, append);
    }

}
