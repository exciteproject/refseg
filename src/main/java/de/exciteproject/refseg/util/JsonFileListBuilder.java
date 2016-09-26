package de.exciteproject.refseg.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class JsonFileListBuilder {

    public static void main(String[] args) {
	File inputFileDirectory = new File(args[0]);
	File outputFile = new File(args[1]);
	double percentage = 0.5;

	JsonFileListBuilder.writeSubList(inputFileDirectory, outputFile, percentage);
    }

    /**
     *
     * @param inputFileDirectory
     * @param outputFile
     * @param percentage
     *            percentage of inputFileDirectory that should be kept
     */
    public static void writeSubList(File inputFileDirectory, File outputFile, double percentage) {
	List<File> inputFiles = Arrays.asList(inputFileDirectory.listFiles());

	List<File> filteredFiles = ListHelper.getRandomSubList(inputFiles, percentage);

	JsonHelper.writeToFile(filteredFiles, outputFile);
    }
}
