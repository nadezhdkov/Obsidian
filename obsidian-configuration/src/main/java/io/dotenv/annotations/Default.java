package io.dotenv.annotations;

import java.lang.annotation.*;

/**
 * Defines a default value if the environment variable does not exist.
 *
 * <pre>
 * @Env("DB_PORT")
 * @Default("3306")
 * int port;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface Default {

    /**
     * Default value.
     */
    String value();
}
