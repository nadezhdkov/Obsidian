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

package obsidian.util.concurrent.atomic;

import obsidian.util.concurrent.Box;

import java.util.concurrent.atomic.AtomicReference;

public final class AtomicBox<T> implements Box<T> {
    private final AtomicReference<T> reference;

    public AtomicBox(T initial) {
        this.reference = new AtomicReference<>(initial);
    }

    @Override
    public T get() {
        return reference.get();
    }

    @Override
    public void set(T value) {
        reference.set(value);
    }

    @Override
    public T getAndSet(T value) {
        return reference.getAndSet(value);
    }

    @Override
    public boolean compareAndSet(T expect, T update) {
        return reference.compareAndSet(expect, update);
    }
}