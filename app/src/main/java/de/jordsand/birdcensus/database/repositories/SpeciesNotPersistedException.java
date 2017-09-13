package de.jordsand.birdcensus.database.repositories;

/**
 * @author Rico Bergmann
 */
public class SpeciesNotPersistedException extends IllegalStateException {

    public SpeciesNotPersistedException() {
    }

    public SpeciesNotPersistedException(String s) {
        super(s);
    }

    public SpeciesNotPersistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpeciesNotPersistedException(Throwable cause) {
        super(cause);
    }
}
