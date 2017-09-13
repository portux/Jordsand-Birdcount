package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import de.jordsand.birdcensus.infrastructure.Repository;

/**
 * Repository to store and retrieve {@link BirdCount} instances
 * @author Rico Bergmann
 */
public interface BirdCountRepository extends Repository<BirdCount, Long> {

    /**
     * Retrieves the bird count with the given start date
     * @param startDate the date to query for
     * @return the matching bird count, or {@code null} if none exists
     */
    @Nullable
    BirdCount findByStartDate(@NonNull Date startDate);

    /**
     * Retrieves all bird counts where the observer participated
     * @param observerName the observer to query for
     * @return the matching bird counts
     */
    @NonNull
    Iterable<BirdCount> findByObserver(@NonNull String observerName);

    /**
     * Checks, whether a bird count took place at a certain time
     * @param startDate the date the census started
     * @return {@code true} if a bird count took place, {@code false} otherwise
     */
    boolean exists(@NonNull Date startDate);

}
