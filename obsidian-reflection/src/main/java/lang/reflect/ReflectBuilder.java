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

package lang.reflect;

import lang.reflect.exception.ReflectException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ReflectBuilder<T> {

    private final Class<T>            type;
    private final Map<String, Object> values;

    private ReflectBuilder(Class<T> type) {
        this.type   = type;
        this.values = new HashMap<>();
    }

    @Contract(value = "_ -> new", pure = true)
    public static <T> @NotNull ReflectBuilder<T> of(Class<T> type) {
        return new ReflectBuilder<>(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull ReflectBuilder<T> from(@NotNull T instance) {
        var builder = new ReflectBuilder<>((Class<T>) instance.getClass());

        Reflect.on(instance)
                .fields()
                .notStatic()
                .each(f -> {
                    try {
                        builder.set(f.getName(), f.get(instance));
                    } catch (Exception ignored) {}
                });

        return builder;
    }

    public ReflectBuilder<T> set(String field, Object value) {
        values.put(field, value);
        return this;
    }

    public ReflectBuilder<T> setAll(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }

    public ReflectBuilder<T> setIfNotNull(String field, Object value) {
        if (value != null) set(field, value);
        return this;
    }

    public ReflectBuilder<T> configure(Consumer<T> configurator) {
        var instance = build();
        configurator.accept(instance);
        return from(instance);
    }

    public ReflectBuilder<T> unset(String field) {
        values.remove(field);
        return this;
    }

    public ReflectBuilder<T> clear() {
        values.clear();
        return this;
    }

    public T build() {
        try {
            var instance = type.getDeclaredConstructor().newInstance();
            injectValues(instance);
            return instance;
        } catch (Exception e) {
            throw new ReflectException("Failed to build " + type.getName(), e);
        }
    }

    public T build(Object... args) {
        try {
            var types = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
            var instance = type.getDeclaredConstructor(types).newInstance(args);
            injectValues(instance);
            return instance;
        } catch (Exception e) {
            throw new ReflectException("Failed to build with args", e);
        }
    }

    public boolean has(String field) {
        return values.containsKey(field);
    }

    public Object get(String field) {
        return values.get(field);
    }

    public Map<String, Object> values() {
        return new HashMap<>(values);
    }

    private void injectValues(T instance) {
        var reflect = Reflect.on(instance);

        values.forEach((name, val) -> {
            try {
                reflect.field(name).set(val);
            } catch (Exception e) {
                throw new ReflectException("Failed to set field: " + name, e);
            }
        });
    }
}