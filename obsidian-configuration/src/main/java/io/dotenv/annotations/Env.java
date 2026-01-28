package io.dotenv.annotations;

import java.lang.annotation.*;

/**
 * Explicitly defines the name of the environment variable to be read.
 *
 * <pre>
 * @Env("DB_HOST")
 * String host;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface Env {

    /**
     * Name of the environment variable.
     */
    String value();
}
