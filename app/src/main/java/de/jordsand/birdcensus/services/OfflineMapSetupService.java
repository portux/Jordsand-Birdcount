package de.jordsand.birdcensus.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple service to prepare offline maps.
 *
 * Many map frameworks expect their offline sources to be located at a certain place. This class
 * should help automate the setup.
 * @author Rico Bergmann
 */
public interface OfflineMapSetupService {

    /**
     * @param offlineMapSource the path to the sources
     * @param destination the path to copy the sources to
     * @throws IOException if the sources could not be copied to the destination folder
     */
    void setup(String offlineMapSource, String destination) throws IOException;

    /**
     * @param offlineMapSource the path to the sources
     * @param destination the path to copy the sources to
     * @throws IOException if the sources could not be copied to the destination folder
     */
    void setup(InputStream offlineMapSource, String destination) throws IOException;

}
