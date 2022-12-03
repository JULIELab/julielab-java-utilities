package de.julielab.java.utilities.index;

public class IndexCreationException extends Exception {
    public IndexCreationException() {
    }

    public IndexCreationException(String message) {
        super(message);
    }

    public IndexCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexCreationException(Throwable cause) {
        super(cause);
    }

    public IndexCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
