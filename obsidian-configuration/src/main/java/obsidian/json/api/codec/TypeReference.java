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

package obsidian.json.api.codec;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Type reference for capturing generic type information at runtime.
 *
 * <p>Java's type erasure removes generic type information at runtime.
 * TypeRef provides a way to capture and preserve this information using
 * anonymous subclasses.</p>
 *
 * <p>Usage examples:</p>
 * <pre>{@code
 * // For simple types
 * TypeRef<User> userType = TypeRef.of(User.class);
 *
 * // For generic types (anonymous subclass)
 * TypeRef<List<User>> users = new TypeRef<List<User>>() {};
 * TypeRef<Map<String, User>> userMap = new TypeRef<Map<String, User>>() {};
 *
 * // Using convenience methods
 * TypeRef<List<User>> users = TypeRef.listOf(User.class);
 * TypeRef<Set<String>> strings = TypeRef.setOf(String.class);
 * TypeRef<Map<String, Integer>> map = TypeRef.mapOf(String.class, Integer.class);
 * }</pre>
 *
 * @param <T> the type being referenced
 * @since 1.0.0
 */
@Getter
public abstract class TypeReference<T> {

    private final Type type;

    /**
     * Creates a type reference by capturing the generic type parameter.
     *
     * <p>This constructor must be called from an anonymous subclass or
     * subclass constructor to properly capture the type.</p>
     *
     * @throws IllegalArgumentException if called without proper type information
     */
    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "TypeRef must be created with type parameter, e.g., new TypeRef<List<String>>() {}"
            );
        }

        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * Creates a type reference from an explicit Type.
     *
     * @param type the type to reference
     */
    private TypeReference(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        this.type = type;
    }

    /**
     * Creates a TypeRef for a simple (non-generic) class.
     *
     * @param <T> the type
     * @param clazz the class
     * @return a type reference
     * @throws IllegalArgumentException if clazz is null
     */
    @Contract("null -> fail; !null -> new")
    public static <T> @NotNull TypeReference<T> of(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return new TypeReference<>(clazz) {
        };
    }

    /**
     * Creates a TypeRef for List&lt;E&gt;.
     *
     * @param <E> the element type
     * @param elementType the element class
     * @return a type reference for List&lt;E&gt;
     * @throws IllegalArgumentException if elementType is null
     */
    @Contract("null -> fail; !null -> new")
    public static <E> @NotNull TypeReference<List<E>> listOf(Class<E> elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("Element type cannot be null");
        }
        return new TypeReference<>(
                new ParameterizedTypeAdapter(List.class, elementType)
        ) {
        };
    }

    /**
     * Creates a TypeRef for Set&lt;E&gt;.
     *
     * @param <E> the element type
     * @param elementType the element class
     * @return a type reference for Set&lt;E&gt;
     * @throws IllegalArgumentException if elementType is null
     */
    @Contract("null -> fail; !null -> new")
    public static <E> @NotNull TypeReference<Set<E>> setOf(Class<E> elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("Element type cannot be null");
        }
        return new TypeReference<>(
                new ParameterizedTypeAdapter(Set.class, elementType)
        ) {
        };
    }

    /**
     * Creates a TypeRef for Map&lt;K, V&gt;.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param keyType the key class
     * @param valueType the value class
     * @return a type reference for Map&lt;K, V&gt;
     * @throws IllegalArgumentException if keyType or valueType is null
     */
    @Contract("null, _ -> fail; !null, null -> fail; !null, !null -> new")
    public static <K, V> @NotNull TypeReference<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        if (keyType == null || valueType == null) {
            throw new IllegalArgumentException("Key and value types cannot be null");
        }
        return new TypeReference<>(
                new ParameterizedTypeAdapter(Map.class, keyType, valueType)
        ) {
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeReference<?> typeReference = (TypeReference<?>) o;
        return type.equals(typeReference.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "TypeRef{" + type + "}";
    }

    /**
     * Internal implementation of ParameterizedType.
     */
    private static class ParameterizedTypeAdapter implements ParameterizedType {

        private final Type rawType;
        private final Type[] actualTypeArguments;

        ParameterizedTypeAdapter(Type rawType, Type... actualTypeArguments) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public Type @NotNull [] getActualTypeArguments() {
            return actualTypeArguments.clone();
        }

        @Override
        public @NotNull Type getRawType() {
            return rawType;
        }

        @Contract(pure = true)
        @Override
        public @Nullable Type getOwnerType() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ParameterizedType that)) return false;
            return rawType.equals(that.getRawType()) &&
                    Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments) ^ rawType.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(rawType.getTypeName());
            if (actualTypeArguments.length > 0) {
                sb.append("<");
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(actualTypeArguments[i].getTypeName());
                }
                sb.append(">");
            }
            return sb.toString();
        }
    }
}