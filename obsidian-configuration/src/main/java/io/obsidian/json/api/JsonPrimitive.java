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

package io.obsidian.json.api;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents a JSON primitive value (string, number, or boolean).
 *
 * <p>This class wraps primitive Java values and provides type-safe
 * access methods with appropriate conversions.</p>
 *
 * @since 1.0.0
 */
public final class JsonPrimitive extends JsonElement {

    private final Object value;

    /**
     * Creates a String primitive.
     *
     * @param string the string value
     * @throws IllegalArgumentException if string is null
     */
    public JsonPrimitive(String string) {
        if (string == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }
        this.value = string;
    }

    /**
     * Creates a Number primitive.
     *
     * @param number the number value
     * @throws IllegalArgumentException if number is null
     */
    public JsonPrimitive(Number number) {
        if (number == null) {
            throw new IllegalArgumentException("Number value cannot be null");
        }
        this.value = number;
    }

    /**
     * Creates a Boolean primitive.
     *
     * @param bool the boolean value
     * @throws IllegalArgumentException if bool is null
     */
    public JsonPrimitive(Boolean bool) {
        if (bool == null) {
            throw new IllegalArgumentException("Boolean value cannot be null");
        }
        this.value = bool;
    }

    /**
     * Creates a Character primitive.
     *
     * @param c the character value
     * @throws IllegalArgumentException if c is null
     */
    public JsonPrimitive(Character c) {
        if (c == null) {
            throw new IllegalArgumentException("Character value cannot be null");
        }
        this.value = c.toString();
    }

    /**
     * Checks if this primitive is a string.
     *
     * @return true if this is a string
     */
    public boolean isString() {
        return value instanceof String;
    }

    /**
     * Checks if this primitive is a number.
     *
     * @return true if this is a number
     */
    public boolean isNumber() {
        return value instanceof Number;
    }

    /**
     * Checks if this primitive is a boolean.
     *
     * @return true if this is a boolean
     */
    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    /**
     * Gets this primitive as a String.
     *
     * @return the string value
     */
    public String asString() {
        if (isNumber()) {
            return getAsNumber().toString();
        } else if (isBoolean()) {
            return Boolean.toString(getAsBoolean());
        }
        return (String) value;
    }

    /**
     * Gets this primitive as a Number.
     *
     * @return the number value
     * @throws UnsupportedOperationException if this is not a number
     */
    public Number getAsNumber() {
        if (value instanceof Number) {
            return (Number) value;
        }

        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new UnsupportedOperationException("Cannot parse as number: " + value, e);
            }
        }
        throw new UnsupportedOperationException("Not a number: " + value);
    }

    /**
     * Gets this primitive as a boolean.
     *
     * @return the boolean value
     */
    public boolean getAsBoolean() {
        if (isBoolean()) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(asString());
    }

    /**
     * Gets this primitive as an int.
     *
     * @return the int value
     * @throws UnsupportedOperationException if conversion fails
     */
    public int asInt() {
        return isNumber() ? getAsNumber().intValue() : Integer.parseInt(asString());
    }

    /**
     * Gets this primitive as a long.
     *
     * @return the long value
     * @throws UnsupportedOperationException if conversion fails
     */
    public long asLong() {
        return isNumber() ? getAsNumber().longValue() : Long.parseLong(asString());
    }

    /**
     * Gets this primitive as a double.
     *
     * @return the double value
     * @throws UnsupportedOperationException if conversion fails
     */
    public double asDouble() {
        return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(asString());
    }

    /**
     * Gets this primitive as a float.
     *
     * @return the float value
     * @throws UnsupportedOperationException if conversion fails
     */
    public float asFloat() {
        return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(asString());
    }

    /**
     * Gets this primitive as a byte.
     *
     * @return the byte value
     * @throws UnsupportedOperationException if conversion fails
     */
    public byte asByte() {
        return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(asString());
    }

    /**
     * Gets this primitive as a short.
     *
     * @return the short value
     * @throws UnsupportedOperationException if conversion fails
     */
    public short asShort() {
        return isNumber() ? getAsNumber().shortValue() : Short.parseShort(asString());
    }

    /**
     * Gets this primitive as a BigInteger.
     *
     * @return the BigInteger value
     * @throws UnsupportedOperationException if conversion fails
     */
    public @NotNull BigInteger asBigInteger() {
        return isNumber() ? new BigInteger(getAsNumber().toString()) : new BigInteger(asString());
    }

    /**
     * Gets this primitive as a BigDecimal.
     *
     * @return the BigDecimal value
     * @throws UnsupportedOperationException if conversion fails
     */
    public @NotNull BigDecimal asBigDecimal() {
        return isNumber() ? new BigDecimal(getAsNumber().toString()) : new BigDecimal(asString());
    }

    @Override
    public JsonElement deepCopy() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPrimitive that = (JsonPrimitive) o;

        if (value instanceof Number && that.value instanceof Number) {
            double thisValue = ((Number) value).doubleValue();
            double thatValue = ((Number) that.value).doubleValue();
            return Double.compare(thisValue, thatValue) == 0;
        }

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        if (value instanceof Number) {
            long v = ((Number) value).longValue();
            return (int) (v ^ (v >>> 32));
        }
        return value.hashCode();
    }
}