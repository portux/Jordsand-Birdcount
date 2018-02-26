package de.jordsand.birdcensus.core;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.Locale;

import de.jordsand.birdcensus.util.Assert;

/**
 * Wrapper for all relevant data of an observation
 */
public class Observation {

    private final Species species;
    private final MonitoringArea location;
    private final int count;

    /**
     * Creates a new observation for the given data
     * @param species the observed species
     * @param location the place where the species was seen
     * @param count the number of species seen
     * @return the observation
     */
    @NonNull
    public static Observation of(@NonNull Species species, @NonNull MonitoringArea location, @IntRange(from = 1) int count) {
        return new Observation(species, location, count);
    }

    /**
     * Creates a new observation
     * @param species the observed species
     * @param location the place where the species was seen
     * @param count the number of species seen
     */
    private Observation(@NonNull Species species, @NonNull MonitoringArea location, @IntRange(from = 1) int count) {
        Assert.noNullParams(species, location);
        Assert.positive(count);
        this.species = species;
        this.location = location;
        this.count = count;
    }

    /**
     * @return the observed species
     */
    public final Species getSpecies() {
        return species;
    }

    /**
     * @return the place where the species was seen
     */
    public final MonitoringArea getLocation() {
        return location;
    }

    /**
     * @return the number of species observed
     */
    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Observation that = (Observation) o;

        if (count != that.count) return false;
        if (!species.equals(that.species)) return false;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        int result = species.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"%s: %s (%d)", location.getName(), species.getName(), count);
    }

}
