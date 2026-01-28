package io.dotenv.annotations;

import java.lang.annotation.*;

/**
 * Indicates that the environment variable is required.
 * If it does not exist, the system must throw an error.
 *
 * <pre>
 * @Env("API_KEY")
 * @RequiredEnv
 * String apiKey;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface RequiredEnv {

    /**
     * Optional custom error message.
     */
    String message() default "";
}
