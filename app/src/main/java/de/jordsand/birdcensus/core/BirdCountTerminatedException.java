package de.jordsand.birdcensus.core;

/**
 * Exception to indicate that a bird count is terminated and may therefore not be modified anymore
 * @author Rico Bergmann
 */

public class BirdCountTerminatedException extends IllegalStateException {
    private final BirdCount birdCount;

    public BirdCountTerminatedException(BirdCount birdCount) {
        this.birdCount = birdCount;
    }

    public BirdCountTerminatedException(BirdCount birdCount, String message) {
        super(message);
        this.birdCount = birdCount;
    }

    public BirdCount getCausingBirdCount() {
        return birdCount;
    }

    @Override
    public String toString() {
        return "BirdCountTerminatedException{" +
                "message=" + getMessage() +
                ", birdCount=" + birdCount +
                '}';
    }
}
