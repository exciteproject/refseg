package de.mkrnr.rse.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class FileHelper {
    public static String readFile(File file) throws IOException {
	return FileHelper.readFile(file, Charset.defaultCharset());
    }

    public static String readFile(File file, Charset encoding) throws IOException {
	byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
	return new String(encoded, encoding);
    }

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
