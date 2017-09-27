package de.jordsand.birdcensus.core;

/**
 * Exception to indicate that a species with that name is already saved
 * @author Rico Bergmann
 */
public class ExistingSpeciesException extends RuntimeException {
    public ExistingSpeciesException() {
    }

    public ExistingSpeciesException(String message) {
        super(message);
    }

    public ExistingSpeciesException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistingSpeciesException(Throwable cause) {
        super(cause);
    }

}
