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

package obsidian.json.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a path within a JSON document.
 *
 * <p>This class is used to track the location of errors within JSON structures,
 * making it easier to identify and fix problems in JSON data.</p>
 *
 * <p>Examples of JSON paths:</p>
 * <ul>
 *   <li>$.user.name</li>
 *   <li>$.items[0].id</li>
 *   <li>$.config.database.connection.timeout</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class JsonPath {

    private final List<String> segments;

    /**
     * Creates an empty JSON path (root).
     */
    public JsonPath() {
        this.segments = new ArrayList<>();
    }

    /**
     * Creates a JSON path from segments.
     *
     * @param segments the path segments
     */
    private JsonPath(List<String> segments) {
        this.segments = new ArrayList<>(segments);
    }

    /**
     * Creates an empty JSON path.
     *
     * @return an empty path
     */
    public static JsonPath root() {
        return new JsonPath();
    }

    /**
     * Appends a field name to this path.
     *
     * @param field the field name
     * @return a new path with the field appended
     * @throws IllegalArgumentException if field is null
     */
    public JsonPath field(String field) {
        if (field == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        List<String> newSegments = new ArrayList<>(segments);
        newSegments.add(field);
        return new JsonPath(newSegments);
    }

    /**
     * Appends an array index to this path.
     *
     * @param index the array index
     * @return a new path with the index appended
     * @throws IllegalArgumentException if index is negative
     */
    public JsonPath index(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative");
        }
        List<String> newSegments = new ArrayList<>(segments);
        newSegments.add("[" + index + "]");
        return new JsonPath(newSegments);
    }

    /**
     * Returns the path segments.
     *
     * @return unmodifiable list of segments
     */
    public List<String> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Checks if this path is empty (root).
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * Returns the number of segments in this path.
     *
     * @return the segment count
     */
    public int size() {
        return segments.size();
    }

    @Override
    public String toString() {
        if (segments.isEmpty()) {
            return "$";
        }
        StringBuilder sb = new StringBuilder("$");
        for (String segment : segments) {
            if (segment.startsWith("[")) {
                sb.append(segment);
            } else {
                sb.append(".").append(segment);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPath jsonPath = (JsonPath) o;
        return segments.equals(jsonPath.segments);
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }
}