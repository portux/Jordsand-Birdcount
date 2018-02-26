package de.jordsand.birdcensus.services.census;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.Iterator;
import java.util.Map;

import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.Species;

/**
 * Provides convenient access to the summarized observations of a bird count
 * @see BirdCount#getObservationSummary()
 */
public class BirdCountSummaryIterator implements Iterator<Pair<Species, Integer>> {

    private Iterator<Map.Entry<Species, Integer>> iterator;

    /**
     * Creates a new {@link Iterator}
     * @param birdCount the census to iterate over
     * @return the iterator
     */
    @NonNull
    public static BirdCountSummaryIterator forCensus(@NonNull BirdCount birdCount) {
        return new BirdCountSummaryIterator(birdCount);
    }

    /**
     * Creates a new {@link Iterator}
     * @param birdCount the census to iterate over
     */
    private BirdCountSummaryIterator(@NonNull BirdCount birdCount) {
        this.iterator = birdCount.getObservationSummary().entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Pair<Species, Integer> next() {
        Map.Entry nextEntry = iterator.next();
        return new Pair<>((Species) nextEntry.getKey(), (Integer) nextEntry.getValue());
    }

}
