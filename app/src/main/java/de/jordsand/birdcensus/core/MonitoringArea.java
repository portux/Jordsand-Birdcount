package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;

/**
 * An area that will be observed during a bird count.
 * Each area is being identified through a unique code and a "plaintext" name.
 * @author Rico Bergmann
 */
public class MonitoringArea {
    private String name;
    private String code;
    private Location location;

    public MonitoringArea(@NonNull String name, @NonNull String code, @NonNull Location location) {
        this.name = name;
        this.code = code;
        this.location = location;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonitoringArea that = (MonitoringArea) o;

        return code.equals(that.code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
