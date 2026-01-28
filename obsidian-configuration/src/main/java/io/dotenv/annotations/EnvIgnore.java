package io.dotenv.annotations;

import java.lang.annotation.*;

/**
 * Marks a field to be ignored by the environment scanner.
 *
 * <pre>
 * @EnvIgnore
 * String temp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface EnvIgnore {
}
