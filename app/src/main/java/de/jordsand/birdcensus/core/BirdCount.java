package de.jordsand.birdcensus.core;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jordsand.birdcensus.util.DateConverter;

/**
 * Representation of a single bird count, just a POJO. Fields should be self-explanatory.
 * @author Rico Bergmann
 */
public class BirdCount {

    private final DateConverter dateConverter = new DateConverter();
    private Date startTime;
    private Date endTime;
    private WeatherData weatherInfo;
    private String observerName;
    private Map<MonitoringArea, WatchList> observedSpecies;

    /**
     * Constructor for starting a new bird count
     * @param startTime the start time
     * @param observerName the name(s) of the person(s) monitoring
     * @param weatherData the weather during the census
     */
    public BirdCount(@NonNull Date startTime, @NonNull String observerName, @NonNull WeatherData weatherData) {
        this.startTime = startTime;
        this.weatherInfo = weatherData;
        this.observerName = observerName;
        this.observedSpecies = new HashMap<>();
    }

    /**
     * Constructor for re-instantiating an already-passed bird count
     * @param startTime the time the census did start
     * @param endTime the time the census did finish
     * @param observerName the name(s) of the person(s) who did the monitoring
     * @param weatherData the weather during the census
     * @param observedSpecies the species that where recorded
     * @throws IllegalArgumentException if {@code startTime > endTime}
     */
    public BirdCount(@NonNull Date startTime, @NonNull Date endTime, @NonNull String observerName,
                     @NonNull WeatherData weatherData, @NonNull Map<MonitoringArea, WatchList> observedSpecies) {
        if(startTime.after(endTime)) {
            throw new IllegalArgumentException(String.format("Start time must be before end time (start: %s end: %s)", startTime, endTime));
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.weatherInfo = weatherData;
        this.observerName = observerName;
        this.observedSpecies = observedSpecies;
    }

    @NonNull
    public Date getStartTime() {
        return startTime;
    }

    @Nullable
    public Date getEndTime() {
        return endTime;
    }

    @NonNull
    public WeatherData getWeatherInfo() {
        return weatherInfo;
    }

    @NonNull
    public String getObserverName() {
        return observerName;
    }

    @NonNull
    public Map<MonitoringArea, WatchList> getObservedSpecies() {
        return Collections.unmodifiableMap(observedSpecies);
    }

    /**
     * @return each observed species and the number of observed individuals
     */
    public Map<Species, Integer> getObservationSummary() {
        List<Map<Species, Integer>> rawObservationData = getObservedSpeciesMaps();
        return Collections.unmodifiableMap(mergeObservedSpeciesMap(rawObservationData));
    }

    /**
     * @return the number of different species observed
     */
    public int getDifferentSpeciesCount() {
        Set<Species> observedSpecies = new HashSet<>();
        for (WatchList watchList : this.observedSpecies.values()) {
            observedSpecies.addAll(watchList.getObservedSpecies());
        }
        return observedSpecies.size();
    }

    /**
     * @return the total number of individuals observed
     */
    public int getTotalObservedSpeciesCount() {
        int totalCount = 0;
        for (Integer  count : getObservationSummary().values()) {
            totalCount += count;
        }
        return totalCount;
    }

    /**
     * @param species the species to count
     * @return the total number of individuals of the given species observed during this bird count
     */
    public int getObservedCountOf(Species species) {
        int count = 0;
        for (WatchList w : observedSpecies.values()) {
            Integer curr = w.getObservedSpeciesMap().get(species);
            if (curr != null) {
                count += curr;
            }
        }
        return count;
    }

    /**
     * @param species the species to count
     * @param areaCode the code of the area to observe
     * @return the total number of individuals observed in the given area
     */
    public int getObservedCountOf(Species species, String areaCode) {
        for (MonitoringArea monitoringArea : observedSpecies.keySet()) {
            if (monitoringArea.getCode().equals(areaCode)) {
                Integer count = observedSpecies.get(monitoringArea).getObservedSpeciesMap().get(species);
                return (count != null) ? count : 0;
            }
        }
        return 0;
    }

    /**
     * @param species the species to count
     * @param area the area to observe
     * @return the total number of individuals observed in the given area
     */
    public int getObservedCountOf(Species species, MonitoringArea area) {
        WatchList w = observedSpecies.get(area);
        if (w != null) {
            Integer count = w.getObservedSpeciesMap().get(species);
            return (count != null) ? count : 0;
        }
        return 0;
    }

    /**
     * @param species the species to query for
     * @return {@code true} if the species was observed during this bird count, {@code false} otherwise
     */
    public boolean wasObserved(Species species) {
        for (MonitoringArea area : observedSpecies.keySet()) {
            if (wasObservedIn(species, area)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param species the species to query for
     * @param areaCode the code of the area to check
     * @return {@code true} if the species was observed in the given area, {@code false} otherwise
     */
    public boolean wasObservedIn(Species species, String areaCode) {
        for (MonitoringArea area : observedSpecies.keySet()) {
            if (area.getCode().equals(areaCode)) {
                return wasObservedIn(species, area);
            }
        }
        return false;
    }

    /**
     * @param species the species to query for
     * @param area the area to check
     * @return {@code true} if the species was observed in the given area, {@code false} otherwise
     */
    public boolean wasObservedIn(Species species, MonitoringArea area) {
        WatchList watchList = observedSpecies.get(area);
        if (watchList != null) {
            Integer count = watchList.getObservedSpeciesMap().get(species);
            return (count != null && count > 0);
        }
        return false;
    }

    /**
     * @return {@code true} if the census is already finished
     */
    public boolean isTerminated()  {
        return endTime != null;
    }

    /**
     * Closes the count. No more species may be recorded afterwards
     * @throws BirdCountTerminatedException if the bird count has already terminated
     */
    public void terminate() {
        if (isTerminated()) {
            throw new BirdCountTerminatedException();
        }
        this.endTime = new Date();
    }

    /**
     * Saves a new record.
     * @param place the area where the species was recorded
     * @param species the species that was seen
     * @param count the number of instances seen
     * @throws BirdCountTerminatedException if the bird count has already terminated
     */
    public void addToWatchlist(@NonNull MonitoringArea place, @NonNull Species species, @IntRange(from = 1) int count) {
        if (isTerminated()) {
            throw new BirdCountTerminatedException();
        } else if (!observedSpecies.containsKey(place)) {
            WatchList list = new WatchList();
            list.addSightingFor(species, count);
            observedSpecies.put(place, list);
        } else {
            observedSpecies.get(place).addSightingFor(species, count);
        }
    }

    /**
     * Extracts the observations from each watchlist
     * @return a list consisting of all observations
     */
    private List<Map<Species, Integer>> getObservedSpeciesMaps() {
        List<Map<Species, Integer>> observedSpeciesMap = new ArrayList<>(observedSpecies.size());
        for (WatchList watchList : observedSpecies.values()) {
            observedSpeciesMap.add(watchList.getObservedSpeciesMap());
        }
        return observedSpeciesMap;
    }

    /**
     * Merges a list of observations into a single map.
     * For each species it will contain the total number of observations
     * @param observedSpecies the raw observation list
     * @return the merged list
     */
    private Map<Species, Integer> mergeObservedSpeciesMap(List<Map<Species, Integer>> observedSpecies) {
        Map<Species, Integer> mergedMap = new HashMap<>();

        for (Map<Species, Integer> watchlist : observedSpecies) {
            for (Species species : watchlist.keySet()) {
                if (mergedMap.containsKey(species)) {
                    mergedMap.put(species, mergedMap.get(species) + watchlist.get(species));
                } else {
                    mergedMap.put(species, watchlist.get(species));
                }
            }
        }
        return mergedMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BirdCount birdCount = (BirdCount) o;

        if (!startTime.equals(birdCount.startTime)) return false;
        if (endTime != null ? !endTime.equals(birdCount.endTime) : birdCount.endTime != null)
            return false;
        return observerName.equals(birdCount.observerName);

    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + observerName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String str;
        if (isTerminated()) {
            str = String.format("Bird count [by=%s; start=%tc; end=%tc]", observerName, startTime, endTime);
        } else {
            str = String.format("Bird count [by=%s; started=%tc; ongoing]", observerName, startTime);
        }
        return str;
    }
}
