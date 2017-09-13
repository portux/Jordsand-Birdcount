package de.jordsand.birdcensus.database.repositories;

/**
 * @author Rico Bergmann
 */
public class BirdCountNotPersistedException extends IllegalStateException {

    public BirdCountNotPersistedException() {
    }

    public BirdCountNotPersistedException(String s) {
        super(s);
    }

    public BirdCountNotPersistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BirdCountNotPersistedException(Throwable cause) {
        super(cause);
    }
}
