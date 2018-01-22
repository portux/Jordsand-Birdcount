package de.jordsand.birdcensus.services.maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.jordsand.birdcensus.util.Files;

/**
 * Service to prepare the offline sources for the <a href="https://github.com/osmdroid/osmdroid">osmdroid</a> library
 * @author Rico Bergmann
 * @see <a href="https://github.com/osmdroid/osmdroid/wiki/Offline-Map-Tiles">https://github.com/osmdroid/osmdroid/wiki/Offline-Map-Tiles</a>
 */
public class OsmDroidOfflineMapSetupService implements OfflineMapSetupService {

    @Override
    public void setup(String offlineMapSource, String destination) throws IOException {
        setup(new FileInputStream(offlineMapSource), destination);
    }

    @Override
    public void setup(InputStream offlineMapSource, String destination) throws IOException {
        File dest = new File(destination);
        if (!dest.exists() && !dest.createNewFile()) {
            throw new IOException("Could not create file " + destination);
        }
        Files.copyFile(offlineMapSource, new FileOutputStream(dest));
    }
}
