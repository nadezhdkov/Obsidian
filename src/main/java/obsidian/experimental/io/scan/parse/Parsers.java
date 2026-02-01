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

package obsidian.experimental.io.scan.parse;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Factory utilities for common {@link Parser} implementations.
 *
 * <h2>Overview</h2>
 * {@code Parsers} provides a collection of reusable parsers for common Java types.
 *
 * <p>
 * All parsers returned by this class:
 * <ul>
 *   <li>are stateless</li>
 *   <li>are safe to reuse</li>
 *   <li>trim input when appropriate</li>
 *   <li>throw {@link ParseFailureException} on failure</li>
 * </ul>
 *
 * <h2>Supported parsers</h2>
 * <ul>
 *   <li>{@link #string()}</li>
 *   <li>{@link #i32()}</li>
 *   <li>{@link #i64()}</li>
 *   <li>{@link #f64()}</li>
 *   <li>{@link #bool()}</li>
 *   <li>{@link #ch()}</li>
 *   <li>{@link #bigInt()}</li>
 *   <li>{@link #bigDec()}</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * int port = scan.read("Port: ", Parsers.i32());
 *
 * boolean enabled = scan.read("Enabled? ", Parsers.bool());
 * }</pre>
 *
 * <h2>Boolean parsing</h2>
 * The boolean parser accepts multiple representations:
 * <ul>
 *   <li>true: {@code true, t, 1, yes, y, sim, s}</li>
 *   <li>false: {@code false, f, 0, no, n, nao, não}</li>
 * </ul>
 *
 * @see Parser
 * @see ParseFailureException
 */
public final class Parsers {

    private Parsers() {}

    /**
     * Returns a parser that returns the raw input unchanged.
     */
    @Contract(pure = true)
    public static @NotNull Parser<String> string() {
        return raw -> raw;
    }

    /**
     * Parses a 32-bit integer.
     *
     * @throws ParseFailureException if the input is not a valid integer.
     */
    @Contract(pure = true)
    public static @NotNull Parser<Integer> i32() {
        return raw -> {
            try { return Integer.parseInt(raw.trim()); }
            catch (Exception e) { throw new ParseFailureException("Expected int", e); }
        };
    }

    /**
     * Parses a 64-bit integer.
     *
     * @throws ParseFailureException if the input is not a valid long.
     */
    @Contract(pure = true)
    public static @NotNull Parser<Long> i64() {
        return raw -> {
            try { return Long.parseLong(raw.trim()); }
            catch (Exception e) { throw new ParseFailureException("Expected long", e); }
        };
    }

    /**
     * Parses a 64-bit floating-point number.
     *
     * @throws ParseFailureException if the input is not a valid double.
     */
    @Contract(pure = true)
    public static @NotNull Parser<Double> f64() {
        return raw -> {
            try { return Double.parseDouble(raw.trim()); }
            catch (Exception e) { throw new ParseFailureException("Expected double", e); }
        };
    }

    /**
     * Parses a boolean value from common textual representations.
     *
     * <p>
     * Accepted true values:
     * {@code true, t, 1, yes, y, sim, s}
     *
     * <p>
     * Accepted false values:
     * {@code false, f, 0, no, n, nao, não}
     *
     * @throws ParseFailureException if the value cannot be interpreted as boolean.
     */
    @Contract(pure = true)
    public static @NotNull Parser<Boolean> bool() {
        return raw -> {
            String v = raw.trim().toLowerCase();
            return switch (v) {
                case "true", "t", "1", "yes", "y", "sim", "s" -> true;
                case "false", "f", "0", "no", "n", "nao", "não" -> false;
                default -> throw new ParseFailureException("Expected boolean");
            };
        };
    }

    /**
     * Parses a single character.
     *
     * @throws ParseFailureException if the trimmed input does not contain exactly one character.
     */
    @Contract(pure = true)
    public static @NotNull Parser<Character> ch() {
        return raw -> {
            String t = raw.trim();
            if (t.length() != 1) throw new ParseFailureException("Expected single char");
            return t.charAt(0);
        };
    }

    /**
     * Parses a {@link BigInteger}.
     */
    @Contract(pure = true)
    public static @NotNull Parser<BigInteger> bigInt() {
        return raw -> {
            try { return new BigInteger(raw.trim()); }
            catch (Exception e) { throw new ParseFailureException("Expected BigInteger", e); }
        };
    }

    /**
     * Parses a {@link BigDecimal}.
     */
    @Contract(pure = true)
    public static @NotNull Parser<BigDecimal> bigDec() {
        return raw -> {
            try { return new BigDecimal(raw.trim()); }
            catch (Exception e) { throw new ParseFailureException("Expected BigDecimal", e); }
        };
    }
}