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

package obsidian.json.api;

/**
 * Represents the JSON null value.
 *
 * <p>This is a singleton class that represents JSON's null value.
 * Use {@link #INSTANCE} to reference the null value.</p>
 *
 * @since 1.0.0
 */
public final class JsonNull extends JsonElement {

    /**
     * The singleton instance of JsonNull.
     */
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @Override
    public JsonElement deepCopy() {
        return INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof JsonNull;
    }

    @Override
    public int hashCode() {
        return JsonNull.class.hashCode();
    }
}