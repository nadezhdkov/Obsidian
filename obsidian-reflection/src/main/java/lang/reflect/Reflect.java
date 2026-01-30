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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Reflect {

    private final Class<?> type;
    private       Object   object;

    private Reflect(Class<?> type) {
        this.type = type;
    }

    @Contract(pure = true)
    private Reflect(@NotNull Object object) {
        this.object = object;
        this.type   = object.getClass();
    }

    @Contract("null -> fail; !null -> new")
    public static @NotNull Reflect on(Class<?> type) {
        if (type == null) throw new IllegalArgumentException("Class cannot be null");
        return new Reflect(type);
    }

    @Contract("null -> fail; !null -> new")
    public static @NotNull Reflect on(Object object) {
        if (object == null) throw new IllegalArgumentException("Instance cannot be null");
        return new Reflect(object);
    }

    @SuppressWarnings("unchecked")
    public <T> T create() {
        try {
            return (T) type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ReflectException("Failed to create instance of " + type.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Object... args) {
        try {
            var types = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
            return (T) type.getDeclaredConstructor(types).newInstance(args);
        } catch (Exception e) {
            throw new ReflectException("Failed to create instance with args", e);
        }
    }

    public Field field(String name) {
        return new Field(this, name);
    }

    public Fields fields() {
        return new Fields(this);
    }

    public ReflectMethod method(String name) {
        return new ReflectMethod(this, name);
    }

    public ReflectMethods methods() {
        return new ReflectMethods(this);
    }

    public ReflectAnnotations annotations() {
        return new ReflectAnnotations(type);
    }

    public Class<?> type() {
        return type;
    }

    public Object object() {
        return object;
    }

    public Reflect bind(Object object) {
        this.object = object;
        return this;
    }

    public boolean isInterface() {
        return type.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(type.getModifiers());
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public boolean isAnnotation() {
        return type.isAnnotation();
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return type.isAnnotationPresent(annotation);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return type.getAnnotation(annotation);
    }

    public Reflect superclass() {
        var parent = type.getSuperclass();
        if (parent == null) throw new ReflectException("No superclass found for " + type.getName());
        return new Reflect(parent);
    }

    public List<Class<?>> interfaces() {
        return Arrays.asList(type.getInterfaces());
    }

    @SuppressWarnings("unchecked")
    public <T> T proxy(@NotNull Class<T> iface, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{ iface },
                handler
        );
    }
}
