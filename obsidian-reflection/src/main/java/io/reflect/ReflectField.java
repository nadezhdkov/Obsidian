package io.reflect;

import io.reflect.exception.ReflectException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectField {

    private final Reflect parent;
    private final Field   field;

    public ReflectField(Reflect parent, String name) {
        this.parent = parent;
        this.field  = findField(name);
    }

    private @NotNull Field findField(String name) {
        var type = parent.type();

        while (type != null) {
            try {
                var f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new ReflectException("Field not found: " + name);
    }

    public <T> T get() {
        return get(parent.object());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object instance) {
        if (instance == null && !isStatic()) {
            throw new ReflectException("Cannot get non-static field without instance");
        }
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new ReflectException("Failed to get field: " + field.getName(), e);
        }
    }

    public ReflectField set(Object value) {
        return set(parent.object(), value);
    }

    public ReflectField set(Object instance, Object value) {
        if (instance == null && !isStatic()) {
            throw new ReflectException("Cannot set non-static field without instance");
        }
        try {
            field.set(instance, value);
            return this;
        } catch (IllegalAccessException e) {
            throw new ReflectException("Failed to set field: " + field.getName(), e);
        }
    }

    public boolean isStatic()    {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isFinal()     {
        return Modifier.isFinal(field.getModifiers());
    }

    public boolean isPublic()    {
        return Modifier.isPublic(field.getModifiers());
    }

    public boolean isPrivate()   {
        return Modifier.isPrivate(field.getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(field.getModifiers());
    }

    public ReflectField removeFinal() {
        try {
            var modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            return this;
        } catch (Exception e) {
            throw new ReflectException("Failed to remove final modifier", e);
        }
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return field.isAnnotationPresent(annotation);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return field.getAnnotation(annotation);
    }

    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    public Class<?> type() {
        return field.getType();
    }

    public String name() {
        return field.getName();
    }

    public boolean isNull() {
        return get() == null;
    }

    public boolean isNotNull() {
        return get() != null;
    }

    public ReflectField copyTo(@NotNull ReflectField target) {
        return target.set(this.get());
    }

    public ReflectField copyTo(Object targetInstance, String targetName) {
        var value = this.get();
        Reflect.on(targetInstance).field(targetName).set(value);
        return this;
    }

    public Field unwrap() {
        return field;
    }

    public Reflect end() {
        return parent;
    }
}