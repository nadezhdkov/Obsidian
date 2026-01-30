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
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Fields {

    private final Reflect               parent;
    private final List<java.lang.reflect.Field>           fields;
    private final List<Predicate<java.lang.reflect.Field>> filters;

    public Fields(@NotNull Reflect parent) {
        this.parent  = parent;
        this.fields  = collectAllFields(parent.type());
        this.filters = new ArrayList<>();
    }

    private @NotNull List<java.lang.reflect.Field> collectAllFields(Class<?> type) {
        var all = new ArrayList<java.lang.reflect.Field>();
        while (type != null) {
            for (var f : type.getDeclaredFields()) {
                f.setAccessible(true);
                all.add(f);
            }
            type = type.getSuperclass();
        }
        return all;
    }

    public Fields filter(Predicate<java.lang.reflect.Field> predicate) {
        filters.add(predicate);
        return this;
    }

    public Fields named(String name)        {
        return filter(f -> f.getName().equals(name));
    }

    public Fields match(String regex)       {
        return filter(f -> f.getName().matches(regex));
    }

    public Fields type(Class<?> type)       {
        return filter(f -> f.getType().equals(type));
    }

    public Fields assignable(Class<?> type) {
        return filter(f -> type.isAssignableFrom(f.getType()));
    }

    public Fields annotated(Class<? extends Annotation> annotation) {
        return filter(f -> f.isAnnotationPresent(annotation));
    }

    public Fields notAnnotated(Class<? extends Annotation> annotation) {
        return filter(f -> !f.isAnnotationPresent(annotation));
    }

    public Fields isPublic()    {
        return filter(f -> Modifier.isPublic(f.getModifiers()));
    }

    public Fields isPrivate()   {
        return filter(f -> Modifier.isPrivate(f.getModifiers()));
    }

    public Fields isProtected() {
        return filter(f -> Modifier.isProtected(f.getModifiers()));
    }

    public Fields isStatic()    {
        return filter(f -> Modifier.isStatic(f.getModifiers()));
    }

    public Fields isFinal()     {
        return filter(f -> Modifier.isFinal(f.getModifiers()));
    }

    public Fields isTransient() {
        return filter(f -> Modifier.isTransient(f.getModifiers()));
    }

    public Fields notStatic()    {
        return filter(f -> !Modifier.isStatic(f.getModifiers()));
    }

    public Fields notFinal()     {
        return filter(f -> !Modifier.isFinal(f.getModifiers()));
    }

    public Fields notTransient() {
        return filter(f -> !Modifier.isTransient(f.getModifiers()));
    }

    public Fields each(Consumer<java.lang.reflect.Field> action) {
        stream().forEach(action);
        return this;
    }

    public Fields eachWrapped(Consumer<Field> action) {
        stream().forEach(f -> action.accept(new Field(parent, f.getName())));
        return this;
    }

    public Fields setAll(Object value) {
        var instance = parent.object();
        stream().forEach(f -> {
            try {
                f.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new ReflectException("Failed to set field: " + f.getName(), e);
            }
        });
        return this;
    }

    public Fields copyTo(Object target) {
        var source = parent.object();
        stream().forEach(f -> {
            try {
                var value = f.get(source);
                var targetField = findFieldInHierarchy(target.getClass(), f.getName());
                if (targetField != null) {
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (Exception ignored) {}
        });
        return this;
    }

    public List<java.lang.reflect.Field> list() {
        return stream().collect(Collectors.toList());
    }

    public List<String> names() {
        return stream().map(java.lang.reflect.Field::getName).collect(Collectors.toList());
    }

    public Map<String, Object> map() {
        var instance = parent.object();
        var map      = new HashMap<String, Object>();

        stream().forEach(f -> {
            try {
                map.put(f.getName(), f.get(instance));
            } catch (IllegalAccessException e) {
                throw new ReflectException("Failed to read field: " + f.getName(), e);
            }
        });
        return map;
    }

    public int count() {
        return (int) stream().count();
    }

    public boolean exists() {
        return count() > 0;
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    public Optional<java.lang.reflect.Field> first() {
        return stream().findFirst();
    }

    public Optional<Field> firstWrapped() {
        return first().map(f -> new Field(parent, f.getName()));
    }

    public Fields clear() {
        filters.clear();
        return this;
    }

    public Reflect end() {
        return parent;
    }

    private Stream<java.lang.reflect.Field> stream() {
        return fields.stream()
                .filter(f -> filters.stream().allMatch(p -> p.test(f)));
    }

    private java.lang.reflect.Field findFieldInHierarchy(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}