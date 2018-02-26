package de.jordsand.birdcensus.services.census;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.MonitoringAreaRepository;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.SpeciesRepository;
import de.jordsand.birdcensus.core.WeatherData;

/**
 * A {@link BirdCountService} that does not execute its operations directly, but rather buffers
 * them for later execution on a real service.
 * <p>
 * When connecting to an instance of {@link SimpleBirdCountService} the binding may take place later on.
 * If the functionality of the service is essential for an activity, it may use this proxy until the
 * real service is bound and then call {@link #flush(BirdCountService)} to apply all pending operations.
 * </p>
 */
public class BirdCountServiceProxy implements BirdCountService {

    /**
     * This is the queue for all operations that ought to be applied.
     * For each invoked method we have a corresponding class and will add an instance of it to the
     * queue.
     */
    private Queue<BirdCountOperation> cachedOperations;

    /**
     * Just the constructor
     */
    public BirdCountServiceProxy() {
        this.cachedOperations = new LinkedList<>();
    }

    /**
     * Applies all pending operations
     * @param realService the real service to use
     */
    public void flush(BirdCountService realService) {
        while(!cachedOperations.isEmpty()) {
            cachedOperations.poll().apply(realService);
        }
    }

    @Nullable @Override
    public BirdCount getCurrentBirdCount() {
        throw new UnsupportedOperationException();
    }

    @NonNull @Override
    public MonitoringAreaRepository getAreaRepository() {
        throw new UnsupportedOperationException();
    }

    @NonNull @Override
    public SpeciesRepository getSpeciesRepository() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startBirdCount(@NonNull Date startDate, @NonNull String observerName, @NonNull WeatherData weatherData) {
        cachedOperations.add(new StartBirdCountOperation(startDate, observerName, weatherData));
    }

    @Override
    public void addSightingToCurrentBirdCount(@NonNull String areaCode, @NonNull Species species, int count) {
        cachedOperations.add(new AddSightingOperation(areaCode, species, count));
    }

    @Nullable @Override
    public Species addNewSpecies(@NonNull String name, @Nullable String scientificName) {
        cachedOperations.add(new AddNewSpeciesOperation(name, scientificName));
        return new Species(name, scientificName);
    }

    @Override
    public void terminateBirdCount() {
        cachedOperations.add(new TerminateBirdCountOperation());
    }

    @Override
    public void abortBirdCount() {
        cachedOperations.add(new AbortBirdCountOperation());
    }

    @Override
    public boolean isBirdCountOngoing() {
        throw new UnsupportedOperationException();
    }

    /**
     * Base class for all operations
     */
    private abstract class BirdCountOperation {

        /**
         * Executes the operation on a (real) service instance
         * @param birdCountService the service to use
         */
        abstract void apply(BirdCountService birdCountService);
    }

    /**
     * Wrapper for the {@link BirdCountService#startBirdCount(Date, String, WeatherData)} method
     */
    private class StartBirdCountOperation extends BirdCountOperation {
        private Date startDate;
        private String observerName;
        private WeatherData weatherData;

        /**
         * We need start date, observer name and weather data
         */
        StartBirdCountOperation(Date startDate, String observerName, WeatherData weatherData) {
            this.startDate = startDate;
            this.observerName = observerName;
            this.weatherData = weatherData;
        }

        @Override
        void apply(BirdCountService birdCountService) {
            birdCountService.startBirdCount(startDate, observerName, weatherData);
        }
    }

    /**
     * Wrapper for the {@link BirdCountService#addSightingToCurrentBirdCount(String, Species, int)} method
     */
    private class AddSightingOperation extends BirdCountOperation {
        private String areaCode;
        private Species species;
        private int count;

        /**
         * We need area, species and count
         */
        AddSightingOperation(String areaCode, Species species, int count) {
            this.areaCode = areaCode;
            this.species = species;
            this.count = count;
        }

        @Override
        void apply(BirdCountService birdCountService) {
            birdCountService.addSightingToCurrentBirdCount(areaCode, species, count);
        }
    }

    /**
     * Wrapper for the {@link BirdCountService#addNewSpecies(String, String)} method
     */
    private class AddNewSpeciesOperation extends BirdCountOperation {
        private String name;
        private String scientificName;

        /**
         * We need name and scientific name
         */
        AddNewSpeciesOperation(String name, String scientificName) {
            this.name = name;
            this.scientificName = scientificName;
        }

        @Override
        void apply(BirdCountService birdCountService) {
            birdCountService.addNewSpecies(name, scientificName);
        }
    }

    /**
     * Wrapper for the {@link BirdCountService#terminateBirdCount()} method
     */
    private class TerminateBirdCountOperation extends  BirdCountOperation {
        @Override
        void apply(BirdCountService birdCountService) {
            birdCountService.terminateBirdCount();
        }
    }

    /**
     * Wrapper for the {@link BirdCountService#abortBirdCount()} method
     */
    private class AbortBirdCountOperation extends BirdCountOperation {
        @Override
        void apply(BirdCountService birdCountService) {
            birdCountService.abortBirdCount();
        }
    }
}
