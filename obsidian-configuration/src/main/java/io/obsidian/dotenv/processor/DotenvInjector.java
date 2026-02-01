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

import io.obsidian.dotenv.annotations.*;
import io.obsidian.dotenv.core.Dotenv;
import io.obsidian.dotenv.processor.exception.DotenvInjectionException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class DotenvInjector {

    private final Dotenv dotenv;

    public DotenvInjector(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    public void inject(@NotNull Object target) {
        var clazz  = target.getClass();
        var prefix = getPrefix(clazz);

        for (var field : clazz.getDeclaredFields()) {
            if (shouldSkip(field)) continue;

            injectField(target, field, prefix);
        }
    }

    private void injectField(Object target, @NotNull Field field, String prefix) {
        field.setAccessible(true);

        var keyName = resolveKey(field, prefix);
        var rawVal  = resolveValue(field, keyName);

        if (rawVal == null) return;

        try {
            var converted = DotenvTypeConverter.convert(rawVal, field.getType());
            field.set(target, converted);
        } catch (Exception e) {
            throw new DotenvInjectionException(
                    "Failed to inject field '%s' (key: %s)".formatted(field.getName(), keyName), e
            );
        }
    }

    private String resolveValue(Field field, String key) {
        var val = dotenv.get(key);

        if (val == null && field.isAnnotationPresent(Default.class)) {
            val = field.getAnnotation(Default.class).value();
        }

        if (val == null && field.isAnnotationPresent(RequiredEnv.class)) {
            var req = field.getAnnotation(RequiredEnv.class);
            var msg = req.message().isEmpty() ? "Missing required env var: " + key : req.message();
            throw new DotenvInjectionException(msg);
        }

        return val;
    }

    private String resolveKey(@NotNull Field field, @NotNull String prefix) {
        var env   = field.getAnnotation(Env.class);
        var value = env.value();

        return (prefix.isEmpty()) ? value : prefix + value;
    }

    private boolean shouldSkip(@NotNull Field field) {
        return field.isAnnotationPresent(EnvIgnore.class)
                || !field.isAnnotationPresent(Env.class);
    }

    private String getPrefix(@NotNull Class<?> clazz) {
        var annotation = clazz.getAnnotation(EnvPrefix.class);
        return annotation != null ? annotation.value() : "";
    }

}
