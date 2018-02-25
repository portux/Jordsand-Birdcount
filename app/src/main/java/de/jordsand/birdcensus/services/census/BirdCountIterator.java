package de.jordsand.birdcensus.services.census;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.Iterator;
import java.util.Map;

import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.core.Observation;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.WatchList;

/**
 * Provides convenient access to all the observations of a bird count
 */
public class BirdCountIterator implements Iterator<Observation> {

    private Iterator<Map.Entry<MonitoringArea, WatchList>> watchlistIterator;
    private Map.Entry<MonitoringArea, WatchList> currentWatchlist;
    private Iterator<Pair<Species, Integer>> observationsIterator;

    /**
     * Creates a new {@link Iterator}
     * @param birdCount the census to iterate over
     * @return the iterator
     */
    @NonNull
    public static BirdCountIterator forCensus(@NonNull BirdCount birdCount) {
        return new BirdCountIterator(birdCount);
    }

    /**
     * Creates a new {@link Iterator}
     * @param birdCount the census to iterate over
     */
    private BirdCountIterator(@NonNull BirdCount birdCount) {
        this.watchlistIterator = birdCount.getObservedSpecies().entrySet().iterator();
        gotoNextWatchlist();
    }

    @Override
    public boolean hasNext() {
        if (observationsIterator == null) {
            return gotoNextWatchlist();
        }
        return hasNextObservationInCurrentWatchlist() || hasNextWatchlist();
    }

    @Override
    public Observation next() {
        if (!hasNextObservationInCurrentWatchlist()) {
            gotoNextWatchlist();
        }
        Pair<Species, Integer> nextObservation = observationsIterator.next();
        return Observation.of(nextObservation.first, currentWatchlist.getKey(), nextObservation.second);
    }

    /**
     * Proceeds to the next watchlist of the bird count if necessary
     * @return whether the iteration continued to the next watchlist
     */
    private boolean gotoNextWatchlist() {
        if (!hasNextWatchlist()) {
            return false;
        }
        currentWatchlist = watchlistIterator.next();
        observationsIterator = currentWatchlist.getValue().iterator();
        return true;
    }

    /**
     * @return whether there is another watchlist
     */
    private boolean hasNextWatchlist() {
        return watchlistIterator.hasNext();
    }

    /**
     * @return whether there is at least one more observation in the current watchlist
     */
    private boolean hasNextObservationInCurrentWatchlist() {
        return observationsIterator.hasNext();
    }

}
