package de.exciteproject.refseg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Class based on: http://stackoverflow.com/a/16028522/2174538
 */
public class ToFileStreamer {

    /**
     * Streams the content of inputStream to the file outputStream
     *
     * @param inputStream
     * @param outputFile
     * @throws IOException
     */
    public static void streamToFile(InputStream inputStream, File outputFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            IOUtils.copy(inputStream, fileOutputStream);
        }
    }
}