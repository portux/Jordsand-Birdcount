package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A specific species
 * @author Rico Bergmann
 */
public class Species {
    private String name;
    private String scientificName;
    private Group belongsTo;

    public Species(@NonNull String name) {
        this.name = name;
    }

    public Species(@NonNull String name, @NonNull String scientificName) {
        this.name = name;
        this.scientificName = scientificName;
    }

    public Species(@NonNull String name, @NonNull String scientificName, @NonNull Group belongsTo) {
        this.name = name;
        this.scientificName = scientificName;
        this.belongsTo = belongsTo;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(@NonNull String scientificName) {
        this.scientificName = scientificName;
    }

    @Nullable
    public Group getBelonging() {
        return belongsTo;
    }

    public void setBelonging(@NonNull Group belongsTo) {
        this.belongsTo = belongsTo;
    }

    /**
     * @return {@code true} if a scientific name was set for this species, or {@code false} otherwise
     */
    public boolean hasScientificName() {
        return scientificName != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Species species = (Species) o;

        if (this.hasScientificName() && species.hasScientificName()) {
            return this.scientificName.equals(species.scientificName);
        } else {
            return this.name.equals(species.name);
        }
    }

    @Override
    public int hashCode() {
        if (hasScientificName()) {
            return scientificName.hashCode();
        } else {
            return name.hashCode();
        }
    }

    @Override
    public String toString() {
        if (hasScientificName()) {
            return name + " (" + scientificName + ")";
        }
        return name;
    }
}
