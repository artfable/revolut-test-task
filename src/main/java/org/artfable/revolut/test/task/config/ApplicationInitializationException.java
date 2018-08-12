package org.artfable.revolut.test.task.config;

/**
 * Exception that will be thrown if application failed to start.
 *
 * @author artfable
 * 11.08.18
 */
public class ApplicationInitializationException extends RuntimeException {
    public ApplicationInitializationException() {
        super();
    }

    public ApplicationInitializationException(String message) {
        super(message);
    }

    public ApplicationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationInitializationException(Throwable cause) {
        super(cause);
    }

    protected ApplicationInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
