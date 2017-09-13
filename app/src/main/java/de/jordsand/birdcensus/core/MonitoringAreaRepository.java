package de.jordsand.birdcensus.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.jordsand.birdcensus.infrastructure.Repository;

/**
 * Repository to store and retrieve {@link MonitoringArea} instances
 * @author Rico Bergmann
 */
public interface MonitoringAreaRepository extends Repository<MonitoringArea, String> {

    /**
     * Retrieves the monitoring area with the given name
     * @param name the name to query for
     * @return the matching monitoring area, or {@code null} if none exists
     */
    @Nullable
    MonitoringArea findByName(@NonNull String name);
}
