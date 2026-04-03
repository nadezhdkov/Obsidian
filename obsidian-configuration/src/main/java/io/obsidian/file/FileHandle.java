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

package io.obsidian.file;

import io.obsidian.file.attribute.FileAttributes;
import io.obsidian.file.attribute.FileMetadata;
import io.obsidian.file.attribute.FilePermissions;
import io.obsidian.file.hash.FileHasher;
import io.obsidian.file.hash.HashAlgorithm;
import io.obsidian.file.io.FileReader;
import io.obsidian.file.io.FileWriter;
import io.obsidian.file.operation.FileCompressor;
import io.obsidian.file.operation.FileOperations;
import io.obsidian.file.search.FileSearch;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Fluent, modular wrapper for file operations — the core of the Obsidian file module.
 *
 * <p>{@code FileHandle} acts as a thin delegation layer that composes specialized
 * components for reading, writing, attributes, permissions, compression, search,
 * and hashing. This design satisfies the Single Responsibility Principle while
 * preserving the ergonomic fluent API.</p>
 *
 * <h3>Architecture</h3>
 * <pre>
 * FileHandle (delegation core)
 * ├── FileReader       — all read operations
 * ├── FileWriter       — all write operations
 * ├── FileAttributes   — metadata queries + atomic snapshot
 * ├── FilePermissions  — POSIX permissions, ownership
 * ├── FileOperations   — create/delete/copy/move/rename/backup/links
 * ├── FileCompressor   — GZIP compress/decompress
 * ├── FileSearch       — filter/grep/replaceAll/count
 * └── FileHasher       — Strategy-based hashing (static)
 * </pre>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Fluent chain
 * FileHandle.at("config.yml")
 *     .create()
 *     .write("key: value")
 *     .compress("config.yml.gz");
 *
 * // Atomic metadata snapshot
 * FileMetadata meta = FileHandle.at("data.csv").metadata();
 * System.out.println(meta.sizeFormatted());
 * }</pre>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class FileHandle {

    @Getter
    private final Path path;
    private Charset charset = StandardCharsets.UTF_8;

    // Lazy-initialized delegates
    private FileReader      reader;
    private FileWriter      writer;
    private FileAttributes  attributes;
    private FilePermissions permissions;
    private FileOperations  operations;
    private FileCompressor  compressor;
    private FileSearch      search;

    private FileHandle(@NotNull Path path) {
        this.path = path;
    }

    // ── Factory Methods ───────────────────────────────────────────────────

    /**
     * Creates a {@code FileHandle} for the given file path string.
     *
     * @param filePath the file path
     * @return a new {@code FileHandle}
     */
    @Contract("_ -> new")
    public static @NotNull FileHandle at(@NotNull String filePath) {
        return new FileHandle(Paths.get(Objects.requireNonNull(filePath)));
    }

    /**
     * Creates a {@code FileHandle} for the given {@link Path}.
     *
     * @param path the file path
     * @return a new {@code FileHandle}
     */
    @Contract("_ -> new")
    public static @NotNull FileHandle at(@NotNull Path path) {
        return new FileHandle(Objects.requireNonNull(path));
    }

    /**
     * Sets the charset for text operations.
     *
     * @param charset the charset to use
     * @return this handle for fluent chaining
     */
    public @NotNull FileHandle charset(@NotNull Charset charset) {
        this.charset = charset;
        // Invalidate cached delegates that depend on charset
        this.reader = null;
        this.writer = null;
        this.search = null;
        return this;
    }

    // ── Delegate Accessors (lazy initialization) ──────────────────────────

    private @NotNull FileReader reader() {
        if (reader == null) reader = new FileReader(path, charset);
        return reader;
    }

    private @NotNull FileWriter writer() {
        if (writer == null) writer = new FileWriter(path, charset);
        return writer;
    }

    private @NotNull FileAttributes attributes() {
        if (attributes == null) attributes = new FileAttributes(path);
        return attributes;
    }

    private @NotNull FilePermissions permissions() {
        if (permissions == null) permissions = new FilePermissions(path);
        return permissions;
    }

    private @NotNull FileOperations operations() {
        if (operations == null) operations = new FileOperations(path);
        return operations;
    }

    private @NotNull FileCompressor compressor() {
        if (compressor == null) compressor = new FileCompressor(path);
        return compressor;
    }

    private @NotNull FileSearch search() {
        if (search == null) search = new FileSearch(path, charset);
        return search;
    }

    // ── Lifecycle (delegated to FileOperations) ───────────────────────────

    public @NotNull FileHandle createIfNotExists() {
        operations().createIfNotExists();
        return this;
    }

    public @NotNull FileHandle createDirectoriesIfNeeded() {
        operations().createDirectoriesIfNeeded();
        return this;
    }

    public @NotNull FileHandle create() {
        operations().create();
        return this;
    }

    public void delete() {
        operations().delete();
    }

    public @NotNull FileHandle deleteIf(@NotNull Predicate<Path> condition) {
        operations().deleteIf(condition);
        return this;
    }

    // ── Write (delegated to FileWriter) ───────────────────────────────────

    public @NotNull FileHandle write(@NotNull String content, boolean append) {
        writer().write(content, append);
        return this;
    }

    public @NotNull FileHandle write(@NotNull String content) {
        writer().write(content);
        return this;
    }

    public @NotNull FileHandle append(@NotNull String content) {
        writer().append(content);
        return this;
    }

    public @NotNull FileHandle writeLines(@NotNull List<String> lines) {
        writer().writeLines(lines);
        return this;
    }

    public @NotNull FileHandle writeBytes(byte @NotNull [] bytes) {
        writer().writeBytes(bytes);
        return this;
    }

    public @NotNull FileHandle writeObject(@NotNull Serializable object) {
        writer().writeObject(object);
        return this;
    }

    public @NotNull FileHandle clear() {
        writer().clear();
        return this;
    }

    // ── Read (delegated to FileReader) ────────────────────────────────────

    public @NotNull String readAllText() {
        return reader().readAllText();
    }

    public byte @NotNull [] readAllBytes() {
        return reader().readAllBytes();
    }

    public @NotNull List<String> readAllLines() {
        return reader().readAllLines();
    }

    public @NotNull Stream<String> lines() {
        return reader().lines();
    }

    public <T> T readObject(@NotNull Class<T> type) {
        return reader().readObject(type);
    }

    public @NotNull List<String> readFirstLines(int n) {
        return reader().readFirstLines(n);
    }

    public @NotNull List<String> readLastLines(int n) {
        return reader().readLastLines(n);
    }

    // ── Search & Filtering (delegated to FileSearch) ──────────────────────

    public @NotNull List<String> filter(@NotNull Predicate<String> predicate) {
        return search().filter(predicate);
    }

    public @NotNull FileHandle filterAndSave(@NotNull Predicate<String> predicate, @NotNull String targetPath) {
        search().filterAndSave(predicate, targetPath);
        return this;
    }

    public @NotNull List<String> grep(@NotNull String regex) {
        return search().grep(regex);
    }

    public @NotNull FileHandle replaceAll(@NotNull String regex, @NotNull String replacement) {
        search().replaceAll(regex, replacement);
        return this;
    }

    public @NotNull FileHandle processLines(@NotNull Consumer<String> processor) {
        search().processLines(processor);
        return this;
    }

    public long countLines() {
        return search().countLines();
    }

    public long count(@NotNull String searchString) {
        return search().count(searchString);
    }

    public boolean contentEquals(@NotNull String otherPath) {
        return search().contentEquals(otherPath);
    }

    // ── File Manipulation (delegated to FileOperations) ───────────────────

    public @NotNull FileHandle copyTo(@NotNull String targetPath) {
        operations().copyTo(targetPath);
        return this;
    }

    public @NotNull FileHandle copyToIfNotExists(@NotNull String targetPath) {
        operations().copyToIfNotExists(targetPath);
        return this;
    }

    public @NotNull FileHandle moveTo(@NotNull String targetPath) {
        operations().moveTo(targetPath);
        return this;
    }

    public @NotNull FileHandle renameTo(@NotNull String newFileName) {
        var newPath = operations().renameTo(newFileName);
        return FileHandle.at(newPath);
    }

    public @NotNull FileHandle backup() {
        operations().backup();
        return this;
    }

    public @NotNull FileHandle createHardLink(@NotNull String linkPath) {
        operations().createHardLink(linkPath);
        return this;
    }

    public @NotNull FileHandle createSymbolicLink(@NotNull String linkPath) {
        operations().createSymbolicLink(linkPath);
        return this;
    }

    // ── Compression (delegated to FileCompressor) ─────────────────────────

    public @NotNull FileHandle compress(@NotNull String targetPath) {
        compressor().compress(targetPath);
        return this;
    }

    public @NotNull FileHandle decompress(@NotNull String targetPath) {
        compressor().decompress(targetPath);
        return this;
    }

    // ── Attributes (delegated to FileAttributes) ──────────────────────────

    /**
     * Captures an atomic snapshot of all file metadata.
     *
     * @return an immutable {@link FileMetadata} record
     */
    public @NotNull FileMetadata metadata() {
        return attributes().snapshot();
    }

    public boolean exists() {
        return attributes().exists();
    }

    public long size() {
        return attributes().size();
    }

    public @NotNull String sizeFormatted() {
        return attributes().sizeFormatted();
    }

    public boolean isRegularFile()  { return attributes().isRegularFile(); }
    public boolean isDirectory()    { return attributes().isDirectory(); }
    public boolean isSymbolicLink() { return attributes().isSymbolicLink(); }
    public boolean isReadable()     { return attributes().isReadable(); }
    public boolean isWritable()     { return attributes().isWritable(); }
    public boolean isExecutable()   { return attributes().isExecutable(); }
    public boolean isHidden()       { return attributes().isHidden(); }

    public @NotNull String getFileName() {
        return attributes().getFileName();
    }

    public @NotNull String getFileNameWithoutExtension() {
        return attributes().getFileNameWithoutExtension();
    }

    public @NotNull String getExtension() {
        return attributes().getExtension();
    }

    public @NotNull String getAbsolutePath() {
        return attributes().getAbsolutePath();
    }

    public String getParent() {
        return attributes().getParent();
    }

    public @NotNull Instant getLastModifiedTime() {
        return attributes().getLastModifiedTime();
    }

    public @NotNull FileHandle setLastModifiedTime(@NotNull Instant instant) {
        attributes().setLastModifiedTime(instant);
        return this;
    }

    public @NotNull Instant getCreationTime() {
        return attributes().getCreationTime();
    }

    public String getOwner() {
        return attributes().getOwner();
    }

    public boolean isNewerThan(@NotNull String otherPath) {
        return getLastModifiedTime().isAfter(FileHandle.at(otherPath).getLastModifiedTime());
    }

    public boolean isOlderThan(@NotNull String otherPath) {
        return getLastModifiedTime().isBefore(FileHandle.at(otherPath).getLastModifiedTime());
    }

    // ── Permissions (delegated to FilePermissions) ────────────────────────

    public @NotNull FileHandle setOwner(@NotNull String owner) {
        permissions().setOwner(owner);
        return this;
    }

    public @NotNull Set<PosixFilePermission> getPosixPermissions() {
        return permissions().getPosixPermissions();
    }

    public @NotNull FileHandle setPosixPermissions(@NotNull Set<PosixFilePermission> perms) {
        permissions().setPosixPermissions(perms);
        return this;
    }

    public @NotNull FileHandle setReadOnly() {
        permissions().setReadOnly();
        return this;
    }

    public @NotNull FileHandle setWritable() {
        permissions().setWritable();
        return this;
    }

    public @NotNull FileHandle setExecutable() {
        permissions().setExecutable();
        return this;
    }

    // ── Hashing (delegated to FileHasher — static) ────────────────────────

    /**
     * Computes a hash of this file using the given {@link HashAlgorithm} strategy.
     *
     * @param algorithm the hashing strategy
     * @return hex-encoded hash string
     */
    public @NotNull String hash(@NotNull HashAlgorithm algorithm) {
        return FileHasher.hash(path, algorithm);
    }

    /**
     * Computes a hash of this file using a JCA algorithm name.
     *
     * @param algorithmName the algorithm (e.g. {@code "SHA-256"})
     * @return hex-encoded hash string
     */
    public @NotNull String hash(@NotNull String algorithmName) {
        return FileHasher.hash(path, algorithmName);
    }

    // ── Static Utilities ──────────────────────────────────────────────────

    /**
     * Creates a temporary file with the given prefix and suffix.
     *
     * @param prefix the temp file prefix
     * @param suffix the temp file suffix
     * @return a {@code FileHandle} for the newly created temp file
     */
    public static @NotNull FileHandle createTemp(@NotNull String prefix, @NotNull String suffix) {
        return FileHandle.at(FileOperations.createTemp(prefix, suffix));
    }

    /**
     * Checks whether two paths refer to the same file.
     *
     * @param path1 first path
     * @param path2 second path
     * @return {@code true} if both paths resolve to the same file
     */
    public static boolean isSameFile(@NotNull String path1, @NotNull String path2) {
        return FileOperations.isSameFile(path1, path2);
    }

    /**
     * Extracts the extension from a file name string.
     *
     * @param fileName the file name
     * @return the extension without the dot, or empty string
     */
    public static @NotNull String getExtension(@NotNull String fileName) {
        return FileAttributes.extractExtension(fileName);
    }

    /**
     * Probes the MIME type for the given file path.
     *
     * @param filePath the file path string
     * @return the MIME type, or {@code null} if undetermined
     */
    public static String getMimeType(@NotNull String filePath) {
        return FileAttributes.getMimeType(Paths.get(filePath));
    }

    /**
     * Computes a hash for a file at the given path using a JCA algorithm name.
     *
     * @param filePath  the file path
     * @param algorithm the algorithm name
     * @return hex-encoded hash string
     */
    public static @NotNull String getFileHash(@NotNull String filePath, @NotNull String algorithm) {
        return FileHasher.hash(Paths.get(filePath), algorithm);
    }
}
