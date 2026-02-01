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

package obsidian.json.internal.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import obsidian.json.api.JsonElement;
import obsidian.json.api.codec.JsonCodec;
import obsidian.json.internal.gson.GsonElementBridge;

import java.io.IOException;

/**
 * Adapter that bridges Obsidian JsonCodec to Gson TypeAdapter.
 *
 * <p>This allows custom Obsidian codecs to be used within Gson's
 * serialization/deserialization pipeline.</p>
 *
 * @param <T> the type being adapted
 * @since 1.0.0
 */
public final class GsonCodecAdapter<T> extends TypeAdapter<T> {

    private final JsonCodec<T> codec;
    private final TypeAdapter<com.google.gson.JsonElement> elementAdapter;

    public GsonCodecAdapter(JsonCodec<T> codec, TypeAdapter<com.google.gson.JsonElement> elementAdapter) {
        if (codec == null) {
            throw new IllegalArgumentException("Codec cannot be null");
        }
        if (elementAdapter == null) {
            throw new IllegalArgumentException("Element adapter cannot be null");
        }
        this.codec = codec;
        this.elementAdapter = elementAdapter;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        // Use codec to encode to Obsidian JsonElement
        JsonElement obsidianElement = codec.encode(value);

        // Convert to Gson JsonElement
        com.google.gson.JsonElement gsonElement = GsonElementBridge.toGson(obsidianElement);

        // Write using Gson's element adapter
        elementAdapter.write(out, gsonElement);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        // Read using Gson's element adapter
        com.google.gson.JsonElement gsonElement = elementAdapter.read(in);

        if (gsonElement == null || gsonElement.isJsonNull()) {
            return null;
        }

        // Convert to Obsidian JsonElement
        JsonElement obsidianElement = GsonElementBridge.toObsidian(gsonElement);

        // Use codec to decode
        return codec.decode(obsidianElement);
    }
}