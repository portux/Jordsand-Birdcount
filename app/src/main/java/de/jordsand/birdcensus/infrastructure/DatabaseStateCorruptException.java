package de.jordsand.birdcensus.infrastructure;

/**
 * Exception to indicate that some database constraints were violated
 * @author Rico Bergmann
 */
public class DatabaseStateCorruptException extends IllegalStateException {

    public DatabaseStateCorruptException() {
    }

    public DatabaseStateCorruptException(String s) {
        super(s);
    }

    public DatabaseStateCorruptException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseStateCorruptException(Throwable cause) {
        super(cause);
    }
}
