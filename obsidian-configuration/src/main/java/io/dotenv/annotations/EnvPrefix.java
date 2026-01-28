package io.dotenv.annotations;

import java.lang.annotation.*;

/**
 * Defines an automatic prefix for all fields in the class.
 *
 * <pre>
 * @EnvPrefix("REDIS_")
 * class RedisConfig {
 *     @Env("HOST") String host;   // REDIS_HOST
 *     @Env("PORT") int port;     // REDIS_PORT
 * }
 * </pre>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EnvPrefix {

    /**
     * Prefix to be applied.
     */
    String value();
}
