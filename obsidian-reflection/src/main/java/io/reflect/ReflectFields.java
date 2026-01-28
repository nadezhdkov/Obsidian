package io.reflect;

import io.reflect.exception.ReflectException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectFields {

    private final Reflect               parent;
    private final List<Field>           fields;
    private final List<Predicate<Field>> filters;

    public ReflectFields(@NotNull Reflect parent) {
        this.parent  = parent;
        this.fields  = collectAllFields(parent.type());
        this.filters = new ArrayList<>();
    }

    private @NotNull List<Field> collectAllFields(Class<?> type) {
        var all = new ArrayList<Field>();
        while (type != null) {
            for (var f : type.getDeclaredFields()) {
                f.setAccessible(true);
                all.add(f);
            }
            type = type.getSuperclass();
        }
        return all;
    }

    public ReflectFields filter(Predicate<Field> predicate) {
        filters.add(predicate);
        return this;
    }

    public ReflectFields named(String name)        {
        return filter(f -> f.getName().equals(name));
    }

    public ReflectFields match(String regex)       {
        return filter(f -> f.getName().matches(regex));
    }

    public ReflectFields type(Class<?> type)       {
        return filter(f -> f.getType().equals(type));
    }

    public ReflectFields assignable(Class<?> type) {
        return filter(f -> type.isAssignableFrom(f.getType()));
    }

    public ReflectFields annotated(Class<? extends Annotation> annotation) {
        return filter(f -> f.isAnnotationPresent(annotation));
    }

    public ReflectFields notAnnotated(Class<? extends Annotation> annotation) {
        return filter(f -> !f.isAnnotationPresent(annotation));
    }

    public ReflectFields isPublic()    {
        return filter(f -> Modifier.isPublic(f.getModifiers()));
    }

    public ReflectFields isPrivate()   {
        return filter(f -> Modifier.isPrivate(f.getModifiers()));
    }

    public ReflectFields isProtected() {
        return filter(f -> Modifier.isProtected(f.getModifiers()));
    }

    public ReflectFields isStatic()    {
        return filter(f -> Modifier.isStatic(f.getModifiers()));
    }

    public ReflectFields isFinal()     {
        return filter(f -> Modifier.isFinal(f.getModifiers()));
    }

    public ReflectFields isTransient() {
        return filter(f -> Modifier.isTransient(f.getModifiers()));
    }

    public ReflectFields notStatic()    {
        return filter(f -> !Modifier.isStatic(f.getModifiers()));
    }

    public ReflectFields notFinal()     {
        return filter(f -> !Modifier.isFinal(f.getModifiers()));
    }

    public ReflectFields notTransient() {
        return filter(f -> !Modifier.isTransient(f.getModifiers()));
    }

    public ReflectFields each(Consumer<Field> action) {
        stream().forEach(action);
        return this;
    }

    public ReflectFields eachWrapped(Consumer<ReflectField> action) {
        stream().forEach(f -> action.accept(new ReflectField(parent, f.getName())));
        return this;
    }

    public ReflectFields setAll(Object value) {
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

    public ReflectFields copyTo(Object target) {
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

    public List<Field> list() {
        return stream().collect(Collectors.toList());
    }

    public List<String> names() {
        return stream().map(Field::getName).collect(Collectors.toList());
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

    public Optional<Field> first() {
        return stream().findFirst();
    }

    public Optional<ReflectField> firstWrapped() {
        return first().map(f -> new ReflectField(parent, f.getName()));
    }

    public ReflectFields clear() {
        filters.clear();
        return this;
    }

    public Reflect end() {
        return parent;
    }

    private Stream<Field> stream() {
        return fields.stream()
                .filter(f -> filters.stream().allMatch(p -> p.test(f)));
    }

    private Field findFieldInHierarchy(Class<?> clazz, String name) {
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