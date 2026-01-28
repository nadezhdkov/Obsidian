package io.dotenv.processor;

import io.dotenv.annotations.*;
import io.dotenv.core.Dotenv;
import io.dotenv.processor.exception.DotenvInjectionException;
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
