package de.mkrnr.rse.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class FileMerger {

    /**
     * Merges the inputFiles at the end of outputFile.
     *
     * @param inputFiles
     * @param outputFile
     * @return
     */
    public static File mergeFiles(ArrayList<File> inputFiles, File outputFile) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            for (File file : inputFiles) {
                InputStream inputStream;
                inputStream = new FileInputStream(file);
                byteArrayOutputStream.write(inputStream);
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

            byteArrayOutputStream.close();
            ToFileStreamer.streamToFile(byteArrayInputStream, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }
}
