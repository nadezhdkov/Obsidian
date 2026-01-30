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

package io.obsidian.dotenv.processor;

import io.obsidian.dotenv.core.Dotenv;

/**
 * The {@code DotenvBinder} interface provides methods to scan and automatically inject
 * environment variable values into fields of a target object. These fields must be annotated
 * appropriately to indicate the desired injection behavior. The environment variables can
 * be sourced from a `.env` file and the system's environment variables.
 */
public interface DotenvBinder {

    /**
     * Binds environment variables from the default {@link Dotenv} instance to the fields
     * of the specified target object. Fields in the target object are automatically injected
     * with values based on annotations, such as {@code @Env}, defined in the object. By using this
     * method, values from the environment or `.env` file can be seamlessly assigned to the
     * corresponding fields of the target object.
     *
     * @param target the object into which environment variables will be injected.
     *               This object must have fields annotated to enable injection.
     * @throws IllegalArgumentException if the {@code target} object is {@code null}.
     */
    static void bind(Object target) {
        bind(target, Dotenv.load());
    }

    /**
     * Binds environment variables from the provided {@link Dotenv} instance to the fields
     * of the specified target object. Fields in the target object are automatically injected
     * with values based on annotations, such as {@code @Env}, defined in the object. By using this
     * method, values from the environment or `.env` file can be seamlessly assigned to the
     * corresponding fields of the target object.
     *
     * @param target the object into which environment variables will be injected.
     *               This object must have fields annotated to enable injection.
     * @param dotenv the {@link Dotenv} instance providing access to environment variables
     *               from the system or `.env` file.
     * @throws IllegalArgumentException if the {@code target} object or {@code dotenv} is {@code null}.
     */
    static void bind(Object target, Dotenv dotenv) {
        if (target == null) throw new IllegalArgumentException("Target object cannot be null");
        if (dotenv == null) throw new IllegalArgumentException("Dotenv instance cannot be null");

        new DotenvInjector(dotenv).inject(target);
    }

}
