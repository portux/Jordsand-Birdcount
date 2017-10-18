package de.jordsand.birdcensus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Useful helper function for dealing with files
 * @author Rico Bergmann
 */
public class Files {
    private final static int BUFFER_SIZE = 1024;

    /**
     * Copies the contents of a file
     * @param src the source file
     * @param dst the destination file
     * @throws IOException if something went wrong
     */
    public static void copyFile(File src, File dst) throws IOException {
        InputStream inp = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                copyFile(inp, out);
            } finally {
                out.close();
            }
        } finally {
            inp.close();
        }
    }

    /**
     * Copies the contents of a file
     * @param inp the source file
     * @param out the destination file
     * @throws IOException if something went wrong
     */
    public static void copyFile(InputStream inp, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (inp.read(buffer) > 0) {
            out.write(buffer);
        }
    }

    /**
     * Creates a file
     * @param destination the directory containing the file
     * @param fname the file name
     * @return the file
     * @throws IOException if something went wrong
     */
    public static File createFile(String destination, String fname) throws IOException {
        String destName = destination + File.separator + fname;
        File file = new File(destName);
        if (!file.createNewFile()) {
            throw new IOException("Files " + destName + " could not be created");
        }
        return file;
    }
}
