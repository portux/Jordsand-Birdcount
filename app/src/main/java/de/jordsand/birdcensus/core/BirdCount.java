package de.jordsand.birdcensus.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jordsand.birdcensus.util.DateConverter;

/**
 * Representation of a single bird count, just a POJO. Fields should be self-explanatory.
 * @author Rico Bergmann
 */
public class BirdCount implements Parcelable {
    public static final Parcelable.Creator<BirdCount> CREATOR = new BirdCountParcelableCreator();

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

    @Override
    public int describeContents() {
        return 0; // we do not have any special contents, so just return '0'
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // TODO write and retrieve date-instances to parcelable?
        out.writeString(dateConverter.formatDate(startTime));
        out.writeValue(isTerminated());
        if (isTerminated()) {
            out.writeString(dateConverter.formatDate(endTime));
        }

        // TODO make WeatherInfo parcelable

        out.writeString(observerName);

        // *************************************
        // TODO discuss applicability of general (background) Service to store current (ongoing) bird count instead of making everything Parcelable
        // *************************************
        //
        // possible structure:
        //
        // +-------------+
        // | application |
        // +-------------+---------------+
        // | background-service          |
        // +-----------------------------+
        //
        // on init new bird count: propagate to service
        // retrieve current bird count from service
        // when finished store bird count in database
        //



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

    public static class BirdCountParcelableCreator implements Parcelable.Creator<BirdCount> {
        @Override
        public BirdCount createFromParcel(Parcel parcel) {
            return null;
        }

        @Override
        public BirdCount[] newArray(int i) {
            return new BirdCount[0];
        }
    }
}
