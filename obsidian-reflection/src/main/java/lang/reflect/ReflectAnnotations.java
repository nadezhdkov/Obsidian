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
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectAnnotations {

    private final AnnotatedElement element;

    public ReflectAnnotations(AnnotatedElement element) {
        this.element = element;
    }

    public boolean has(Class<? extends Annotation> type) {
        return element.isAnnotationPresent(type);
    }

    public <T extends Annotation> T get(Class<T> type) {
        return element.getAnnotation(type);
    }

    public <T extends Annotation> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(element.getAnnotation(type));
    }

    public List<Annotation> list() {
        return Arrays.asList(element.getAnnotations());
    }

    public <T extends Annotation> List<T> list(Class<T> type) {
        return Arrays.asList(element.getAnnotationsByType(type));
    }

    public Map<String, Annotation> map() {
        return Arrays.stream(element.getAnnotations())
                .collect(Collectors.toMap(
                        a -> a.annotationType().getSimpleName(),
                        a -> a
                ));
    }

    public List<String> names() {
        return Arrays.stream(element.getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .collect(Collectors.toList());
    }

    public List<Class<? extends Annotation>> types() {
        return Arrays.stream(element.getAnnotations())
                .map(Annotation::annotationType)
                .collect(Collectors.toList());
    }

    public int count() {
        return element.getAnnotations().length;
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    public boolean isPresent() {
        return count() > 0;
    }

    public <T> T value(Class<? extends Annotation> type) {
        return value(type, "value");
    }

    @SuppressWarnings("unchecked")
    public <T> T value(Class<? extends Annotation> type, String member) {
        var annotation = get(type);
        if (annotation == null) return null;

        try {
            var method = annotation.annotationType().getMethod(member);
            return (T) method.invoke(annotation);
        } catch (Exception e) {
            throw new ReflectException(
                    "Failed to read value from annotation @" + type.getSimpleName(), e
            );
        }
    }
}