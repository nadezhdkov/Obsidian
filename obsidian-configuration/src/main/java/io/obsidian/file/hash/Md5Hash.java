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

package io.obsidian.file.hash;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * MD5 hashing strategy implementation.
 *
 * <p><strong>Note:</strong> MD5 is not cryptographically secure. Use for
 * checksums and integrity verification only, not for security-sensitive
 * hashing.</p>
 *
 * @since 1.0
 */
public record Md5Hash() implements HashAlgorithm {

    private static final String ALGORITHM = "MD5";

    @Override
    public @NotNull String algorithmName() {
        return ALGORITHM;
    }

    @Override
    public @NotNull String hash(byte @NotNull [] data) {
        try {
            var digest = MessageDigest.getInstance(ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}
