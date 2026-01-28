package io.dotenv.processor.exception;

public class DotenvInjectionException extends RuntimeException {

    public DotenvInjectionException(String message) {
        super(message);
    }

    public DotenvInjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DotenvInjectionException(Throwable cause) {
        super(cause);
    }
}
