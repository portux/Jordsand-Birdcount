package de.jordsand.birdcensus.util;

import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Provides convenient access to some file system related functionality.
 */
public class FileSystem {

    /**
     * @return the directory containing all document files
     */
    public static File getDocumentDirectoryRoot() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return getLegacyDocumentDirectoryRoot();
        } else {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        }
    }

    /**
     * Devices older then the ones shipped with KitKat do not have a dedicated document directory
     * therefore we need to create it ourselves.
     * @return the directory containing all document files
     */
    private static File getLegacyDocumentDirectoryRoot() {
        File docRoot = new File(Environment.getExternalStorageDirectory() + File.pathSeparator + "Documents");
        docRoot.mkdirs(); // yes, we do not care about the result as false means the directory already exists
        return docRoot;
    }

}
