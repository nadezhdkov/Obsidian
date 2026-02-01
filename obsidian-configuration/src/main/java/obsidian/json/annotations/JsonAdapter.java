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

package obsidian.json.annotations;

import obsidian.json.api.codec.JsonCodec;

import java.lang.annotation.*;

/**
 * Specifies a custom codec (serializer/deserializer) for a field.
 *
 * <p>Use this annotation to override the default serialization behavior
 * for a specific field. The codec class must implement {@link JsonCodec}
 * and have a no-argument constructor.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class Order {
 *     @JsonAdapter(UuidCodec.class)
 *     private UUID orderId;
 *
 *     @JsonAdapter(LocalDateCodec.class)
 *     private LocalDate orderDate;
 * }
 *
 * public class UuidCodec implements JsonCodec<UUID> {
 *     @Override
 *     public JsonElement encode(UUID value) {
 *         return new JsonPrimitive(value.toString());
 *     }
 *
 *     @Override
 *     public UUID decode(JsonElement element) {
 *         return UUID.fromString(element.asJsonPrimitive().asString());
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonAdapter {

    /**
     * The codec class to use for this field.
     *
     * @return the codec class
     */
    Class<? extends JsonCodec<?>> value();
}