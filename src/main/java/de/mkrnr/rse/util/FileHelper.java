package de.mkrnr.rse.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileHelper {

    public static void resetDirectory(File directory) {
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException("input is not a directory: " + directory.getAbsolutePath());
        }

        if (directory.exists()) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        directory.mkdirs();

    }
}
