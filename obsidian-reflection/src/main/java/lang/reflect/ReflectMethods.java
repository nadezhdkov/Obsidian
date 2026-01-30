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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectMethods {

    private final Reflect                parent;
    private final List<Method>           methods;
    private final List<Predicate<Method>> filters;

    public ReflectMethods(@NotNull Reflect parent) {
        this.parent  = parent;
        this.methods = collectAllMethods(parent.type());
        this.filters = new ArrayList<>();
    }

    private @NotNull List<Method> collectAllMethods(Class<?> type) {
        var all = new ArrayList<Method>();
        while (type != null) {
            for (var m : type.getDeclaredMethods()) {
                m.setAccessible(true);
                all.add(m);
            }
            type = type.getSuperclass();
        }
        return all;
    }

    public ReflectMethods filter(Predicate<Method> predicate) {
        filters.add(predicate);
        return this;
    }

    public ReflectMethods named(String name)       {
        return filter(m -> m.getName().equals(name));
    }

    public ReflectMethods match(String regex)      {
        return filter(m -> m.getName().matches(regex));
    }

    public ReflectMethods startWith(String prefix) {
        return filter(m -> m.getName().startsWith(prefix));
    }

    public ReflectMethods getters() {
        return filter(m -> (m.getName().startsWith("get") || m.getName().startsWith("is"))
                && m.getParameterCount() == 0
                && !m.getReturnType().equals(Void.TYPE));
    }

    public ReflectMethods setters() {
        return filter(m -> m.getName().startsWith("set") && m.getParameterCount() == 1);
    }

    public ReflectMethods returning(Class<?> type) { return filter(m -> m.getReturnType().equals(type)); }

    public ReflectMethods voidOnly()               {
        return filter(m -> m.getReturnType().equals(Void.TYPE));
    }

    public ReflectMethods params(int count)        {
        return filter(m -> m.getParameterCount() == count);
    }

    public ReflectMethods noParams()               {
        return params(0);
    }

    public ReflectMethods hasParams()              {
        return filter(m -> m.getParameterCount() > 0);
    }

    public ReflectMethods annotated(Class<? extends Annotation> annotation) {
        return filter(m -> m.isAnnotationPresent(annotation));
    }

    public ReflectMethods notAnnotated(Class<? extends Annotation> annotation) {
        return filter(m -> !m.isAnnotationPresent(annotation));
    }

    public ReflectMethods isPublic()    {
        return filter(m -> Modifier.isPublic(m.getModifiers()));
    }

    public ReflectMethods isPrivate()   {
        return filter(m -> Modifier.isPrivate(m.getModifiers()));
    }

    public ReflectMethods isProtected() {
        return filter(m -> Modifier.isProtected(m.getModifiers()));
    }

    public ReflectMethods isStatic()    {
        return filter(m -> Modifier.isStatic(m.getModifiers()));
    }

    public ReflectMethods isAbstract()  {
        return filter(m -> Modifier.isAbstract(m.getModifiers()));
    }

    public ReflectMethods isFinal()     {
        return filter(m -> Modifier.isFinal(m.getModifiers()));
    }

    public ReflectMethods notStatic()   {
        return filter(m -> !Modifier.isStatic(m.getModifiers()));
    }

    public ReflectMethods notAbstract() {
        return filter(m -> !Modifier.isAbstract(m.getModifiers()));
    }

    public ReflectMethods notFinal()    {
        return filter(m -> !Modifier.isFinal(m.getModifiers()));
    }

    public ReflectMethods each(Consumer<Method> action) {
        stream().forEach(action);
        return this;
    }

    public ReflectMethods eachWrapped(Consumer<ReflectMethod> action) {
        stream().forEach(m -> action.accept(
                new ReflectMethod(parent, m.getName()).types(m.getParameterTypes())
        ));
        return this;
    }

    public ReflectMethods invokeAll() {
        var instance = parent.object();
        stream().forEach(m -> {
            try {
                m.invoke(instance);
            } catch (Exception e) {
                throw new ReflectException("Failed to invoke: " + m.getName(), e);
            }
        });
        return this;
    }

    public List<Method> list() {
        return stream().collect(Collectors.toList());
    }

    public List<String> names() {
        return stream().map(Method::getName).collect(Collectors.toList());
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

    public Optional<Method> first() {
        return stream().findFirst();
    }

    public Optional<ReflectMethod> firstWrapped() {
        return first().map(m ->
                new ReflectMethod(parent, m.getName()).types(m.getParameterTypes())
        );
    }

    public ReflectMethods clear() {
        filters.clear();
        return this;
    }

    public Reflect end() {
        return parent;
    }

    private Stream<Method> stream() {
        return methods.stream()
                .filter(m -> filters.stream().allMatch(p -> p.test(m)));
    }
}