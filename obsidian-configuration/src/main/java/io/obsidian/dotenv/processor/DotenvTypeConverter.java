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

package io.obsidian.dotenv.processor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class DotenvTypeConverter {

    private static final Map<Class<?>, TypeConverter<?>> CONVERTERS = new HashMap<>();

    static {
        register(String.class,   val -> val);
        register(Integer.class,  Integer::valueOf);
        register(int.class,      Integer::valueOf);
        register(Long.class,     Long::valueOf);
        register(long.class,     Long::valueOf);
        register(Double.class,   Double::valueOf);
        register(double.class,   Double::valueOf);
        register(Float.class,    Float::valueOf);
        register(float.class,    Float::valueOf);
        register(Boolean.class,  Boolean::parseBoolean);
        register(boolean.class,  Boolean::parseBoolean);
        register(Duration.class, Duration::parse);
        register(Path.class,     Paths::get);
        register(File.class,     File::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T convert(String val, Class<T> type) {
        if (val == null) return null;

        if (CONVERTERS.containsKey(type)) {
            return (T) CONVERTERS.get(type).convert(val);
        }

        if (type.isEnum()) {
            return convertEnum(val, (Class<? extends Enum>) type);
        }

        if (List.class.isAssignableFrom(type)) {
            return (T) convertToList(val, String.class);
        }

        if (Set.class.isAssignableFrom(type)) {
            return (T) convertToSet(val, String.class);
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    public static <T> void register(Class<T> type, TypeConverter<T> converter) {
        CONVERTERS.put(type, converter);
    }

    public static boolean isSupported(Class<?> type) {
        return CONVERTERS.containsKey(type)
                || type.isEnum()
                || List.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> @NotNull T convertEnum(@NotNull String val, Class<? extends Enum> type) {
        return (T) Enum.valueOf(type, val.toUpperCase());
    }

    private static <T> List<T> convertToList(String val, Class<T> type) {
        if (val == null || val.isBlank()) return Collections.emptyList();

        return Arrays.stream(val.split(","))
                .map(String::trim)
                .map(v -> convert(v, type))
                .collect(Collectors.toList());
    }

    @Contract("_, _ -> new")
    private static <T> @NotNull Set<T> convertToSet(String val, Class<T> type) {
        return new HashSet<>(convertToList(val, type));
    }

    @FunctionalInterface
    public interface TypeConverter<T> {
        T convert(String value);
    }
}
