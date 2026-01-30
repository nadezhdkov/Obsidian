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

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("unused")
public class Files {

    @Getter
    private final Path    path;
    private       Charset charset = StandardCharsets.UTF_8;

    private Files(String filePath) {
        this.path = Paths.get(filePath);
    }

    @Contract("_ -> new")
    public static @NotNull Files at(String filePath) {
        return new Files(Objects.requireNonNull(filePath));
    }

    @Contract("_ -> new")
    public static @NotNull Files at(Path path) {
        return new Files(Objects.requireNonNull(path).toString());
    }

    public Files charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Files createIfNotExists() {
        if (java.nio.file.Files.notExists(path)) {
            try {
                if (path.getParent() != null) {
                    java.nio.file.Files.createDirectories(path.getParent());
                }
                java.nio.file.Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public Files createDirectoriesIfNeeded() {
        if (path.getParent() != null) {
            try {
                java.nio.file.Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public Files create() {
        return createDirectoriesIfNeeded().createIfNotExists();
    }

    public void delete() {
        try {
            java.nio.file.Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Files deleteIf(@NotNull Predicate<Path> condition) {
        if (condition.test(path)) {
            delete();
        }
        return this;
    }

    public Files clear() {
        return write("");
    }

    public Files write(@NotNull String content, boolean append) {
        var mode = append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
        try {
            java.nio.file.Files.writeString(path, content, charset, StandardOpenOption.CREATE, mode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files write(@NotNull String content) {
        return write(content, false);
    }

    public Files append(@NotNull String content) {
        return write(content, true);
    }

    public Files writeLines(List<String> lines) {
        try {
            java.nio.file.Files.write(path, lines, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files writeBytes(byte[] bytes) {
        try {
            java.nio.file.Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files writeObject(Serializable object) {
        try (var os  = java.nio.file.Files.newOutputStream(path);
             var oos = new ObjectOutputStream(os)) {
            oos.writeObject(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public String readAllText() {
        try {
            return java.nio.file.Files.readString(path, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readAllBytes() {
        try {
            return java.nio.file.Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> readAllLines() {
        try {
            return java.nio.file.Files.readAllLines(path, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<String> lines() {
        try {
            return java.nio.file.Files.lines(path, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> type) {
        try (var is  = java.nio.file.Files.newInputStream(path);
             var ois = new ObjectInputStream(is)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> readFirstLines(int n) {
        try (var stream = lines()) {
            return stream.limit(n).collect(Collectors.toList());
        }
    }

    public List<String> readLastLines(int n) {
        var allLines = readAllLines();
        int size     = allLines.size();
        return allLines.subList(Math.max(0, size - n), size);
    }

    public List<String> filter(Predicate<String> predicate) {
        try (var stream = lines()) {
            return stream.filter(predicate).collect(Collectors.toList());
        }
    }

    public Files filterAndSave(Predicate<String> predicate, String targetPath) {
        var filtered = filter(predicate);
        Files.at(targetPath).writeLines(filtered);
        return this;
    }

    public List<String> grep(String regex) {
        var pattern = Pattern.compile(regex);
        return filter(line -> pattern.matcher(line).find());
    }

    public Files replaceAll(String regex, String replacement) {
        var content  = readAllText();
        var replaced = content.replaceAll(regex, replacement);
        return write(replaced);
    }

    public Files processLines(Consumer<String> processor) {
        try (var stream = lines()) {
            stream.forEach(processor);
        }
        return this;
    }

    public long countLines() {
        try (var stream = lines()) {
            return stream.count();
        }
    }

    public long count(String searchString) {
        try (var stream = lines()) {
            return stream
                    .mapToLong(line -> (line.length() - line.replace(searchString, "").length()) / searchString.length())
                    .sum();
        }
    }

    public Files copyTo(String targetPath) {
        try {
            java.nio.file.Files.copy(path, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files copyToIfNotExists(String targetPath) {
        var target = Paths.get(targetPath);
        if (java.nio.file.Files.notExists(target)) {
            copyTo(targetPath);
        }
        return this;
    }

    public Files moveTo(String targetPath) {
        try {
            java.nio.file.Files.move(path, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files renameTo(String newFileName) {
        var parent  = path.getParent();
        var newPath = (parent != null) ? parent.resolve(newFileName) : Paths.get(newFileName);
        try {
            java.nio.file.Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Files.at(newPath.toString());
    }

    public Files backup() {
        var backupName =  getFileName() + "." + System.currentTimeMillis() + ".bak";
        var target     = path.getParent().resolve(backupName).toString();
        return copyTo(target);
    }

    public Files createHardLink(String linkPath) {
        try {
            java.nio.file.Files.createLink(Paths.get(linkPath), path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files createSymbolicLink(String linkPath) {
        try {
            java.nio.file.Files.createSymbolicLink(Paths.get(linkPath), path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files compress(String targetPath) {
        try (var in  = java.nio.file.Files.newInputStream(path);
             var out = new GZIPOutputStream(java.nio.file.Files.newOutputStream(Paths.get(targetPath)))) {
            in.transferTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files decompress(String targetPath) {
        try (var in  = new GZIPInputStream(java.nio.file.Files.newInputStream(path));
             var out = java.nio.file.Files.newOutputStream(Paths.get(targetPath))) {
            in.transferTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public boolean exists() {
        return java.nio.file.Files.exists(path);
    }

    public long size() {
        try {
            return java.nio.file.Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String sizeFormatted() {
        long size = size();
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    public boolean isRegularFile() { return java.nio.file.Files.isRegularFile(path); }
    public boolean isDirectory()   { return java.nio.file.Files.isDirectory(path); }
    public boolean isSymbolicLink(){ return java.nio.file.Files.isSymbolicLink(path); }
    public boolean isReadable()    { return java.nio.file.Files.isReadable(path); }
    public boolean isWritable()    { return java.nio.file.Files.isWritable(path); }
    public boolean isExecutable()  { return java.nio.file.Files.isExecutable(path); }

    public boolean isHidden() {
        try {
            return java.nio.file.Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    public String getFileNameWithoutExtension() {
        var name = getFileName();
        var dot  = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    public String getExtension() {
        return getExtension(getFileName());
    }

    public String getAbsolutePath() {
        return path.toAbsolutePath().toString();
    }

    public String getParent() {
        var parent = path.getParent();
        return parent != null ? parent.toString() : null;
    }

    public Instant getLastModifiedTime() {
        try {
            return java.nio.file.Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Files setLastModifiedTime(Instant instant) {
        try {
            java.nio.file.Files.setLastModifiedTime(path, FileTime.from(instant));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Instant getCreationTime() {
        try {
            var attrs = java.nio.file.Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.creationTime().toInstant();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getOwner() {
        try {
            return java.nio.file.Files.getOwner(path).getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Files setOwner(String owner) {
        try {
            var user = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(owner);
            java.nio.file.Files.setOwner(path, user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Set<PosixFilePermission> getPosixPermissions() {
        try {
            return java.nio.file.Files.getPosixFilePermissions(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Files setPosixPermissions(Set<PosixFilePermission> permissions) {
        try {
            java.nio.file.Files.setPosixFilePermissions(path, permissions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Files setReadOnly() {
        var file = path.toFile();
        file.setWritable(false);
        file.setExecutable(false);
        return this;
    }

    public Files setWritable() {
        path.toFile().setWritable(true);
        return this;
    }

    public Files setExecutable() {
        path.toFile().setExecutable(true);
        return this;
    }

    public boolean contentEquals(String otherPath) {
        try {
            byte[] content1 = readAllBytes();
            byte[] content2 = Files.at(otherPath).readAllBytes();
            return Arrays.equals(content1, content2);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNewerThan(String otherPath) {
        return getLastModifiedTime().isAfter(Files.at(otherPath).getLastModifiedTime());
    }

    public boolean isOlderThan(String otherPath) {
        return getLastModifiedTime().isBefore(Files.at(otherPath).getLastModifiedTime());
    }

    public static @NotNull String getExtension(@NotNull String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1);
    }

    public static String getMimeType(String filePath) {
        try {
            return java.nio.file.Files.probeContentType(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileHash(String filePath, String algorithm) {
        try {
            var path   = Paths.get(filePath);
            var data   = java.nio.file.Files.readAllBytes(path);
            var digest = MessageDigest.getInstance(algorithm);
            var hash   = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating hash: " + e.getMessage(), e);
        }
    }

    public static @NotNull Files createTemp(String prefix, String suffix) {
        try {
            return Files.at(java.nio.file.Files.createTempFile(prefix, suffix));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSameFile(String path1, String path2) {
        try {
            return java.nio.file.Files.isSameFile(Paths.get(path1), Paths.get(path2));
        } catch (IOException e) {
            return false;
        }
    }

}
