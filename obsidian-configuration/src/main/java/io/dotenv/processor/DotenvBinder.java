package io.dotenv.processor;

import io.dotenv.core.Dotenv;

/**
 * The {@code DotenvBinder} interface provides methods to scan and automatically inject
 * environment variable values into fields of a target object. These fields must be annotated
 * appropriately to indicate the desired injection behavior. The environment variables can
 * be sourced from a `.env` file and the system's environment variables.
 */
public interface DotenvBinder {

    /**
     * Scans the provided target object and automatically injects values into its fields
     * based on annotations and environment variables retrieved from the `.env` file
     * and system environment using the default configuration of {@code Dotenv}.
     * <p>
     * This method initializes a {@code Dotenv} instance using {@code Dotenv.load()} and
     * subsequently applies the environment variable values to annotated fields in the target object.
     *
     * @param target the object into which environment variables will be injected.
     *               This object should have fields annotated with relevant annotations
     *               that dictate how the injection is performed.
     * @throws IllegalArgumentException if the target object is {@code null}.
     */
    static void bind(Object target) {
        bind(target, Dotenv.load());
    }

    /**
     * Scans the provided target object and injects environment variable values into its fields
     * based on annotations defined within the target class. The environment variables are
     * retrieved using the provided {@code Dotenv} instance.
     *
     * @param target the object into which environment variables will be injected.
     *               Fields that should be injected must have the appropriate annotations.
     * @param dotenv the {@code Dotenv} instance used to load environment variables from
     *               the system and `.env` file.
     * @throws IllegalArgumentException if the target object or the {@code Dotenv} instance is {@code null}.
     */
    static void bind(Object target, Dotenv dotenv) {
        if (target == null) throw new IllegalArgumentException("Target object cannot be null");
        if (dotenv == null) throw new IllegalArgumentException("Dotenv instance cannot be null");

        new DotenvInjector(dotenv).inject(target);
    }

}
