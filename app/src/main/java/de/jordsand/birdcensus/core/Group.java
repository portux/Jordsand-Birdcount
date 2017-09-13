package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;

/**
 * Abstraction of a group of related species
 * @author Rico Bergmann
 */
public class Group {
    private String name;
    private String scientificName;

    public Group(@NonNull String name, @NonNull String scientificName) {
        this.name = name;
        this.scientificName = scientificName;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getScientificName() {
        return scientificName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return scientificName.equals(group.scientificName);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + scientificName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name  + " (" + scientificName + ")";
    }
}
