package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple watchlist for a certain area
 * @author Rico Bergmann
 */
public class WatchList implements Iterable<Pair<Species, Integer>> {
    private Map<Species, Integer> observedSpecies;

    /**
     * Default constructor for new watchlists
     */
    public WatchList() {
        this.observedSpecies = new HashMap<>();
    }

    /**
     * Constructor for re-instantiating watchlists
     * @param observedSpecies the observed species
     */
    public WatchList(@NonNull Map<Species, Integer> observedSpecies) {
        this.observedSpecies = observedSpecies;
    }

    @NonNull
    public Map<Species, Integer> getObservedSpeciesMap() {
        return Collections.unmodifiableMap(observedSpecies);
    }

    @NonNull
    public Set<Species> getObservedSpecies() {
        return Collections.unmodifiableSet(observedSpecies.keySet());
    }

    /**
     * Adds a  new record for a single observation
     * @param species the species seen
     */
    void addSightingFor(Species species) {
        addSightingFor(species, 1);
    }

    /**
     * Adds a new record for a certain amount of species seen
     * @param species the species seen
     * @param count the number of instances seen
     */
    void addSightingFor(Species species, int count) {
        Integer currentCount = observedSpecies.get(species);
        if (currentCount == null) {
            observedSpecies.put(species, count);
        } else {
            observedSpecies.put(species, currentCount + count);
        }
    }

    @Override
    public Iterator<Pair<Species, Integer>> iterator() {
        return new WatchListIterator(this);
    }

    private class WatchListIterator implements Iterator<Pair<Species, Integer>> {
        private Iterator<Species> it;

        public WatchListIterator(WatchList list) {
            this.it = list.observedSpecies.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Pair<Species, Integer> next() {
            Species nxt = it.next();
            return new Pair<>(nxt, observedSpecies.get(nxt));
        }
    }
}
