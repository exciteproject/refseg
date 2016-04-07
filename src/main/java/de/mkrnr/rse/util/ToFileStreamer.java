package de.mkrnr.rse.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Class based on: http://stackoverflow.com/a/16028522/2174538
 */
public class ToFileStreamer {
    public static final String PREFIX = "stream2file";

    public static final String SUFFIX = ".tmp";

    public static void main(String[] args) {

    }

    public static File streamToFile(InputStream inputStream, File outputFile) throws IOException {
        outputFile = File.createTempFile(PREFIX, SUFFIX);
        outputFile.deleteOnExit();
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            IOUtils.copy(inputStream, fileOutputStream);
        }
        return outputFile;
    }

}