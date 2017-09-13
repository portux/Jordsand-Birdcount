package de.jordsand.birdcensus.core;

/**
 * Exception to indicate that a bird count is terminated and may therefore not be modified anymore
 * @author Rico Bergmann
 */

public class BirdCountTerminatedException extends IllegalStateException {
    public BirdCountTerminatedException() {}

    public BirdCountTerminatedException(String message) {
        super(message);
    }

    public BirdCountTerminatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BirdCountTerminatedException(Throwable cause) {
        super(cause);
    }
}
