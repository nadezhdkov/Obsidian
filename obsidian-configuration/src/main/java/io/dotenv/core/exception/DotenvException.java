package io.dotenv.core.exception;

public class DotenvException extends RuntimeException {
    public DotenvException(String message) {
        super(message);
    }

    public DotenvException(String message, Object ... args) {
        super(String.format(message, args));
    }

    public DotenvException(String message, Throwable cause, Object ... args) {
        super(String.format(message, args), cause);
    }

}
