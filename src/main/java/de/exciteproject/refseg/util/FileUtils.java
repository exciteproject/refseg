package de.exciteproject.refseg.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static File getTempFile(String filePrefix, boolean deleteOnExit) {
        String tempFileName = filePrefix + "-" + System.nanoTime();
        File tempFile = null;
        try {
            tempFile = File.createTempFile(tempFileName, ".txt");
            if (deleteOnExit) {
                tempFile.deleteOnExit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    public static String readFile(File file) throws IOException {
        return FileUtils.readFile(file, Charset.defaultCharset());
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return new String(encoded, encoding);
    }

    public static void renameFiles(File fileDirectory, String regex, String replacement) {
        for (File file : fileDirectory.listFiles()) {
            File newFile = new File(file.getAbsolutePath().replaceFirst(regex, replacement));
            file.renameTo(newFile);
        }
    }

    public static void resetDirectory(File directory) {
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException("input is not a directory: " + directory.getAbsolutePath());
        }

        if (directory.exists()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        directory.mkdirs();
    }
}
