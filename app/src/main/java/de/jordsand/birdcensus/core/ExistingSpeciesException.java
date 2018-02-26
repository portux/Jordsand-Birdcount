package de.jordsand.birdcensus.core;

/**
 * Exception to indicate that a species with that name is already saved
 * @author Rico Bergmann
 */
public class ExistingSpeciesException extends RuntimeException {
    private final Species species;

    public ExistingSpeciesException(Species species) {
        this.species = species;
    }

    public ExistingSpeciesException(Species species, String message) {
        super(message);
        this.species = species;
    }

    public Species getSpecies() {
        return species;
    }

    @Override
    public String toString() {
        return "ExistingSpeciesException{" +
                "message=" + getMessage() +
                ", species=" + species +
                '}';
    }

}
