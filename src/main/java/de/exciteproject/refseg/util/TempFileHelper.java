package de.exciteproject.refseg.util;

import java.io.File;
import java.io.IOException;

public class TempFileHelper {
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
}
