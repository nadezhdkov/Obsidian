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
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

@SuppressWarnings("unused")
public class Field {

    private final Reflect parent;
    private final java.lang.reflect.Field field;

    public Field(Reflect parent, String name) {
        this.parent = parent;
        this.field  = findField(name);
    }

    private @NotNull java.lang.reflect.Field findField(String name) {
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

    public Field set(Object value) {
        return set(parent.object(), value);
    }

    public Field set(Object instance, Object value) {
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

    public Field removeFinal() {
        try {
            var modifiers = java.lang.reflect.Field.class.getDeclaredField("modifiers");
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

    public Field copyTo(@NotNull Field target) {
        return target.set(this.get());
    }

    public Field copyTo(Object targetInstance, String targetName) {
        var value = this.get();
        Reflect.on(targetInstance).field(targetName).set(value);
        return this;
    }

    public java.lang.reflect.Field unwrap() {
        return field;
    }

    public Reflect end() {
        return parent;
    }
}