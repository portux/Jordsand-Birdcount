package de.jordsand.birdcensus.core;

import java.util.List;

import de.jordsand.birdcensus.infrastructure.Repository;

/**
 * Repository to store and retrieve {@link Species} instances
 * @author Rico Bergmann
 */
public interface SpeciesRepository extends Repository<Species, Long> {

    /**
     * Retrieves all species with a given name.
     * As many subspecies - which all share the same name - may exist, multiple species may match.
     * @param name the name to query for
     * @return all (sub)species with the given name
     */
    List<Species> findByName(String name);

    /**
     * Retrieves the species with the given name
     * @param scientificName the scientific name to query for
     * @return the matching species, or {@code null} if none exists
     */
    Species findByScientificName(String scientificName);

}
