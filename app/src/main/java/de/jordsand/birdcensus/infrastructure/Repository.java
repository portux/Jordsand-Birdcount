package de.jordsand.birdcensus.infrastructure;

/**
 * A repository provides access to a set of entities of certain kind which may be distinguished
 * through some kind of identifier.
 * @author Rico Bergmann
 */
public interface Repository<Entity, Id> {

    /**
     * Adds a new entity to the repository
     * @param instance the entity to save
     */
    void save(Entity instance);

    /**
     * Checks for a certain entity
     * @param id the entity's ID
     * @return {@code true} if the repository contains the entity or {@code false} otherwise
     */
    boolean exists(Id id);

    /**
     * Queries for a certain entity
     * @param id the identifier of the entity
     * @return the entity associated with that ID or {@code null} if none exists
     */
    Entity findOne(Id id);

    /**
     * @return all saved entities
     */
    Iterable<Entity> findAll();

    /**
     * Deletes an entity from the repository. If the entity does not exist, nothing happens
     * @param id the identifier of the entity
     * @return {@code true} if the entity was removed or {@code false} otherwise
     */
    boolean remove(Id id);
}
