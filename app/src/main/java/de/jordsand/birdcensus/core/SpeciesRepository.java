package de.jordsand.birdcensus.core;

import de.jordsand.birdcensus.infrastructure.Repository;

/**
 * Repository to store and retrieve {@link Species} instances
 * @author Rico Bergmann
 */
public interface SpeciesRepository extends Repository<Species, Long> {
}
