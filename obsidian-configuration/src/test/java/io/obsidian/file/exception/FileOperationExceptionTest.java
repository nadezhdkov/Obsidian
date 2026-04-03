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

package io.obsidian.file.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileOperationException Sealed Hierarchy")
class FileOperationExceptionTest {

    @Test
    @DisplayName("FileReadException should carry path and message")
    void readException() {
        var path = Path.of("test.txt");
        var cause = new java.io.IOException("disk error");
        var ex = new FileReadException(path, cause);

        assertEquals(path, ex.getTargetPath());
        assertTrue(ex.getMessage().contains("test.txt"));
        assertEquals(cause, ex.getCause());
        assertInstanceOf(FileOperationException.class, ex);
    }

    @Test
    @DisplayName("FileWriteException should carry path and message")
    void writeException() {
        var path = Path.of("output.txt");
        var ex = new FileWriteException(path, new java.io.IOException("write error"));

        assertEquals(path, ex.getTargetPath());
        assertTrue(ex.getMessage().contains("output.txt"));
        assertInstanceOf(FileOperationException.class, ex);
    }

    @Test
    @DisplayName("FileNotFoundException should work with null cause")
    void notFoundException() {
        var path = Path.of("missing.txt");
        var ex = new FileNotFoundException(path);

        assertEquals(path, ex.getTargetPath());
        assertNull(ex.getCause());
        assertTrue(ex.getMessage().contains("missing.txt"));
    }

    @Test
    @DisplayName("FileCompressionException should carry path context")
    void compressionException() {
        var path = Path.of("data.gz");
        var ex = new FileCompressionException(path, new java.io.IOException("corrupt"));

        assertEquals(path, ex.getTargetPath());
        assertTrue(ex.getMessage().contains("data.gz"));
    }

    @Test
    @DisplayName("FileHashException should include algorithm in message")
    void hashException() {
        var path = Path.of("secret.bin");
        var ex = new FileHashException(path, "SHA-512", new java.security.NoSuchAlgorithmException());

        assertTrue(ex.getMessage().contains("SHA-512"));
        assertTrue(ex.getMessage().contains("secret.bin"));
        assertEquals(path, ex.getTargetPath());
    }

    @Test
    @DisplayName("All subtypes should be instances of FileOperationException")
    void sealedHierarchy() {
        var path = Path.of("any.txt");
        var cause = new java.io.IOException("test");

        assertInstanceOf(FileOperationException.class, new FileReadException(path, cause));
        assertInstanceOf(FileOperationException.class, new FileWriteException(path, cause));
        assertInstanceOf(FileOperationException.class, new FileNotFoundException(path));
        assertInstanceOf(FileOperationException.class, new FileCompressionException(path, cause));
        assertInstanceOf(FileOperationException.class, new FileHashException(path, "MD5", cause));
    }

    @Test
    @DisplayName("Exceptions should be catchable as FileOperationException")
    void catchAsBase() {
        var path = Path.of("err.txt");
        try {
            throw new FileReadException(path, new java.io.IOException("boom"));
        } catch (FileOperationException e) {
            assertEquals(path, e.getTargetPath());
            assertTrue(e instanceof FileReadException);
        }
    }
}
