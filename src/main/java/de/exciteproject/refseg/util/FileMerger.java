package de.exciteproject.refseg.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class FileMerger {

    /**
     * Merges the inputFiles at the end of outputFile.
     *
     * @param inputFiles
     * @param outputFile
     * @return
     */
    public static File mergeFiles(List<File> inputFiles, File outputFile) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            for (File file : inputFiles) {
                InputStream inputStream;
                inputStream = new FileInputStream(file);
                byteArrayOutputStream.write(inputStream);
                byteArrayOutputStream.write(System.lineSeparator().getBytes());
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
