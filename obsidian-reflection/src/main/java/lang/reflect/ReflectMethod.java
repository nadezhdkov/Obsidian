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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ReflectMethod {

    private final Reflect    parent;
    private final String     name;
    private       Class<?>[] types;
    private       Method     method;

    public ReflectMethod(Reflect parent, String name) {
        this.parent = parent;
        this.name   = name;
    }

    public ReflectMethod types(Class<?>... types) {
        this.types  = types;
        this.method = null;
        return this;
    }

    public <T> T invoke(Object... args) {
        return invokeOn(parent.object(), args);
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeOn(Object instance, Object... args) {
        var m = resolve(args);

        if (instance == null && !Modifier.isStatic(m.getModifiers())) {
            throw new ReflectException("Cannot invoke non-static method without instance");
        }

        try {
            return (T) m.invoke(instance, args);
        } catch (Exception e) {
            throw new ReflectException("Failed to invoke method: " + name, e);
        }
    }

    public <T> T invokeSafe(Object... args) {
        try {
            return invoke(args);
        } catch (Exception e) {
            return null;
        }
    }

    private Method resolve(Object... args) {
        if (method != null) return method;

        if (types == null && args != null && args.length > 0) {
            types = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
        }

        this.method = findMethod();
        return this.method;
    }

    private Method resolve() {
        return resolve(new Object[0]);
    }

    private @NotNull Method findMethod() {
        var targetClass = parent.type();

        try {
            if (types != null) {
                var m = targetClass.getDeclaredMethod(name, types);
                m.setAccessible(true);
                return m;
            }
            return findMethodByName(targetClass);
        } catch (NoSuchMethodException e) {
            throw new ReflectException("Method not found: " + name, e);
        }
    }

    @Contract("null -> fail")
    private @NotNull Method findMethodByName(Class<?> clazz) throws NoSuchMethodException {
        while (clazz != null) {
            for (var m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    m.setAccessible(true);
                    return m;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchMethodException(name);
    }

    public Class<?>   returnType() {
        return resolve().getReturnType();
    }

    public Class<?>[] parameterTypes() {
        return resolve().getParameterTypes();
    }

    public int        parameterCount() {
        return resolve().getParameterCount();
    }

    public String     name() {
        return name;
    }

    public boolean isStatic()    {
        return Modifier.isStatic(resolve().getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(resolve().getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(resolve().getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(resolve().getModifiers());
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(resolve().getModifiers());
    }

    public boolean isFinal() {
        return Modifier.isFinal(resolve().getModifiers());
    }

    public boolean acceptsParameters() {
        return parameterCount() > 0;
    }

    public boolean returnsVoid() {
        return returnType().equals(Void.TYPE);
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return resolve().isAnnotationPresent(annotation);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return resolve().getAnnotation(annotation);
    }

    public Annotation[] getAnnotations() {
        return resolve().getAnnotations();
    }

    public Method unwrap() {
        return resolve();
    }

    public Reflect end() {
        return parent;
    }
}