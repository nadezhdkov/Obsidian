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

package io.obsidian.dotenv.core;

import io.obsidian.dotenv.core.exception.DotenvException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * The {@code Dotenv} interface provides methods to interact with environment variables
 * and entries from a `.env` file. This interface enables the retrieval of individual
 * environment variable values, access to all available entries, and functionality
 * to apply filters to those entries.
 */
public interface Dotenv {

    enum Filter {
        IN_ENV_FILE,
    }

    /**
     * Creates a new instance of {@link DotenvBuilder} with default configuration.
     * The {@link DotenvBuilder} provides a fluent API for customizing the configuration
     * of how `.env` files and environment variables are loaded and processed.
     *
     * @return a new {@link DotenvBuilder} instance to configure and load environment variables.
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull DotenvBuilder configure() {
        return new DotenvBuilder();
    }

    /**
     * Loads a {@link Dotenv} instance using the default configuration settings.
     * The method initializes a {@link DotenvBuilder}, configures it with the default
     * parameters, and invokes its {@code load()} method to parse environment
     * variables from the specified `.env` file and system properties.
     *
     * @return a {@link Dotenv} instance containing environment variable entries
     *         from the `.env` file and the system environment.
     * @throws DotenvException if there is an error during parsing or loading,
     *                         such as a missing or malformed `.env` file.
     */
    static Dotenv load() {
        return new DotenvBuilder().load();
    }

    /**
     * Retrieves a set of all environment variable entries available within this {@link Dotenv} instance.
     * This includes environment variables from both the `.env` file and the system environment.
     *
     * @return a set of {@link DotenvEntry} objects representing all available environment variables.
     */
    Set<DotenvEntry> entries();

    /**
     * Retrieves a set of environment variable entries filtered based on the specified criteria.
     * If the filter is {@code null}, all environment variables (from both the file and the system)
     * are returned. If the filter is {@link Filter#IN_ENV_FILE}, only the entries from the `.env`
     * file are returned.
     *
     * @param filter the filter to apply when retrieving environment variable entries; may be null
     *               to include all entries
     * @return a set of {@link DotenvEntry} objects representing the filtered environment variables
     */
    Set<DotenvEntry> entries(Filter filter);

    /**
     * Retrieves the value associated with the specified environment variable key.
     * Searches for the key in the environment variables loaded by this {@link Dotenv} instance.
     * If the key does not exist, this method returns {@code null}.
     *
     * @param key the name of the environment variable to retrieve
     * @return the value of the environment variable if it exists; otherwise {@code null}
     * @throws NullPointerException if the key is {@code null}
     */
    String get(String key);

    /**
     * Retrieves the value associated with the specified environment variable key.
     * If the key does not exist, the specified default value is returned.
     *
     * @param key the name of the environment variable to retrieve
     * @param defaultValue the value to return if the key does not exist
     * @return the value of the environment variable if it exists; otherwise the default value
     * @throws NullPointerException if the key is {@code null}
     */
    String get(String key, String defaultValue);

}
