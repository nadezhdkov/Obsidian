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

package obsidian.json.util;

import obsidian.json.api.JsonArray;
import obsidian.json.api.JsonElement;
import obsidian.json.api.JsonObject;
import obsidian.json.api.JsonPrimitive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for pretty-printing JSON elements.
 *
 * <p>This class provides methods to format JSON elements in a human-readable
 * way with proper indentation and line breaks.</p>
 *
 * @since 1.0.0
 */
public final class JsonPrettyPrinter {

    private static final String INDENT = "  ";

    private JsonPrettyPrinter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts a JsonElement to a pretty-printed string.
     *
     * @param element the element to print
     * @return the formatted JSON string
     */
    @Contract("null -> fail")
    public static @NotNull String toPrettyString(JsonElement element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        print(element, sb, 0);
        return sb.toString();
    }

    /**
     * Converts a JsonElement to a compact string.
     *
     * @param element the element to print
     * @return the compact JSON string
     */
    @Contract("null -> fail")
    public static @NotNull String toCompactString(JsonElement element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        printCompact(element, sb);
        return sb.toString();
    }

    private static void print(@NotNull JsonElement element, StringBuilder sb, int depth) {
        if (element.isJsonObject()) {
            printObject(element.asJsonObject(), sb, depth);
        } else if (element.isJsonArray()) {
            printArray(element.asJsonArray(), sb, depth);
        } else if (element.isJsonPrimitive()) {
            printPrimitive(element.asJsonPrimitive(), sb);
        } else if (element.isJsonNull()) {
            sb.append("null");
        }
    }

    private static void printObject(@NotNull JsonObject object, StringBuilder sb, int depth) {
        if (object.isEmpty()) {
            sb.append("{}");
            return;
        }

        sb.append("{");
        boolean first = true;
        for (var entry : object.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\n");
            indent(sb, depth + 1);
            sb.append("\"").append(escapeString(entry.getKey())).append("\": ");
            print(entry.getValue(), sb, depth + 1);
            first = false;
        }
        sb.append("\n");
        indent(sb, depth);
        sb.append("}");
    }

    private static void printArray(@NotNull JsonArray array, StringBuilder sb, int depth) {
        if (array.isEmpty()) {
            sb.append("[]");
            return;
        }

        sb.append("[");
        boolean first = true;
        for (JsonElement element : array) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\n");
            indent(sb, depth + 1);
            print(element, sb, depth + 1);
            first = false;
        }
        sb.append("\n");
        indent(sb, depth);
        sb.append("]");
    }

    private static void printPrimitive(@NotNull JsonPrimitive primitive, StringBuilder sb) {
        if (primitive.isString()) {
            sb.append("\"").append(escapeString(primitive.asString())).append("\"");
        } else {
            sb.append(primitive.asString());
        }
    }

    private static void printCompact(@NotNull JsonElement element, StringBuilder sb) {
        if (element.isJsonObject()) {
            printObjectCompact(element.asJsonObject(), sb);
        } else if (element.isJsonArray()) {
            printArrayCompact(element.asJsonArray(), sb);
        } else if (element.isJsonPrimitive()) {
            printPrimitive(element.asJsonPrimitive(), sb);
        } else if (element.isJsonNull()) {
            sb.append("null");
        }
    }

    private static void printObjectCompact(@NotNull JsonObject object, @NotNull StringBuilder sb) {
        sb.append("{");
        boolean first = true;
        for (var entry : object.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeString(entry.getKey())).append("\":");
            printCompact(entry.getValue(), sb);
            first = false;
        }
        sb.append("}");
    }

    private static void printArrayCompact(@NotNull JsonArray array, @NotNull StringBuilder sb) {
        sb.append("[");
        boolean first = true;
        for (JsonElement element : array) {
            if (!first) sb.append(",");
            printCompact(element, sb);
            first = false;
        }
        sb.append("]");
    }

    private static void indent(@NotNull StringBuilder sb, int depth) {
        sb.append(INDENT.repeat(Math.max(0, depth)));
    }

    private static @NotNull String escapeString(@NotNull String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}