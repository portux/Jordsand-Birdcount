package de.jordsand.birdcensus.services;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.ExistingSpeciesException;
import de.jordsand.birdcensus.core.MonitoringAreaRepository;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.SpeciesRepository;
import de.jordsand.birdcensus.core.WeatherData;

/**
 * Service to provide convenient access to a bird count.
 * @author Rico Bergmann
 */
public interface BirdCountService {

    /**
     * @return the current bird count, if no bird count was started, {@code null} is returned
     */
    @Nullable
    BirdCount getCurrentBirdCount();

    /**
     * @return the area repository
     */
    @NonNull
    MonitoringAreaRepository getAreaRepository();

    /**
     * @return the species repository
     */
    @NonNull
    SpeciesRepository getSpeciesRepository();

    /**
     * Initiates a new bird count
     * @param startDate the start date to use
     * @param observerName the involved observers
     * @param weatherData the weather during the bird count
     * @throws IllegalStateException if a bird count was already started
     */
    void startBirdCount(@NonNull Date startDate, @NonNull String observerName, @NonNull WeatherData weatherData);

    /**
     * Adds a new sighting to the current bird count
     * @param areaCode the area where the sighting happened
     * @param species the species observed
     * @param count the number of instances seen
     * @throws IllegalStateException if no bird count was started
     */
    void addSightingToCurrentBirdCount(@NonNull String areaCode, @NonNull Species species, int count);

    /**
     * Saves a new species. It will <strong>not</strong> be added as observation however
     * @param name the species' name
     * @param scientificName the species' scientific name
     * @return the created species
     * @throws ExistingSpeciesException if such a species does already exist
     */
    @Nullable
    Species addNewSpecies(@NonNull String name, @Nullable String scientificName);

    /**
     * Finishes the current bird count
     * @throws IllegalStateException if no bird count is going on
     */
    void terminateBirdCount();

    /**
     * Aborts the current bird count.
     * If no bird count is going on, it will be ignored.
     */
    void abortBirdCount();

    /**
     * @return {@code true} if a bird count was started (and not yet finished), {@code false} otherwise
     */
    boolean isBirdCountOngoing();

}
