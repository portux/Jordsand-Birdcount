package de.jordsand.birdcensus.services.exporter;

import android.support.annotation.NonNull;

import de.jordsand.birdcensus.core.BirdCount;

/**
 * Service to provide the data of a bird count in some different format
 */
public interface BirdCountExporter {

    /**
     * Provides a bird count with all its details (i.e. a detailed list of all observations)
     * @param birdCount the bird count to export
     */
    void exportCompleteBirdCount(@NonNull BirdCount birdCount);

    /**
     * Provides a more concise representation of the bird count (i.e. only the summarized observations)
     * @param birdCount the bird count to export
     */
    void exportBirdCountSummary(@NonNull BirdCount birdCount);

}
