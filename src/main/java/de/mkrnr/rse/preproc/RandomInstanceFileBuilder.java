package de.mkrnr.rse.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RandomInstanceFileBuilder {

    /**
     * Creates an instance file by randomly picking from the passed labels
     *
     * @param inputFiles
     * @param outputFile
     * @param labels
     * @return
     * @throws IOException
     */
    public static File createInstanceFile(List<File> inputFiles, File outputFile, String[] labels) throws IOException {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

	for (File inputFile : inputFiles) {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		String[] lineSplit = line.split("\\s+");
		for (String word : lineSplit) {
		    if (!word.isEmpty()) {
			int labelIndex = (int) (Math.random() * labels.length);
			bufferedWriter.write(word + " " + labels[labelIndex] + System.lineSeparator());
		    }
		}
	    }
	    bufferedReader.close();
	    // empty line to separate the different inputFiles in the merged
	    // outputFile
	    bufferedWriter.write(System.lineSeparator());
	}
	bufferedWriter.close();
	return outputFile;
    }

    /**
     *
     * @param inputFiles
     * @param percentage
     *            percentage of files that should be randomly extracted from
     *            inputFiles
     * @return
     */
    public static List<File> getRandomFiles(List<File> inputFiles, double percentage) {
	if ((percentage < 0) || (percentage > 1)) {
	    throw new IllegalArgumentException("percentage has to be between 0 and 1");
	}

	int numberOfFiles = (int) (inputFiles.size() * percentage);
	return getRandomFiles(inputFiles, numberOfFiles);
    }

    /**
     *
     * @param inputFiles
     * @param numberOfFiles
     *            number of files that should be randomly extracted from
     *            inputFiles
     * @return
     */
    public static List<File> getRandomFiles(List<File> inputFiles, int numberOfFiles) {
	if ((numberOfFiles < 0) || (numberOfFiles > inputFiles.size())) {
	    throw new IllegalArgumentException("numberOfFiles has to be between 0 and size of inputFiles");
	}

	List<File> shuffeledList = new ArrayList<>(inputFiles);
	Collections.shuffle(shuffeledList);
	return shuffeledList.subList(0, numberOfFiles);
    }

    public static void main(String[] args) throws IOException {

	File inputDirectory = new File(args[1]);
	File outputFile = new File(args[2]);
	String[] labels = new String[] { "B-FN", "I-FN", "B-LN", "I-LN", "O", "I-O" };
	if (args[0].equals("create")) {
	    RandomInstanceFileBuilder.createInstanceFile(Arrays.asList(inputDirectory.listFiles()), outputFile, labels);
	} else {
	    if (args[0].equals("merge")) {
		RandomInstanceFileBuilder.mergeInstanceFiles(Arrays.asList(inputDirectory.listFiles()), outputFile);

	    } else {
		throw new IllegalArgumentException("first argument has to be \"create\" or \"merge\"");
	    }
	}
    }

    public static File mergeInstanceFiles(List<File> inputFiles, File outputFile) throws IOException {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

	for (File inputFile : inputFiles) {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		bufferedWriter.write(line + System.lineSeparator());
	    }
	    bufferedReader.close();
	    // empty line to separate the different inputFiles in the merged
	    // outputFile
	    bufferedWriter.write(System.lineSeparator());
	}
	bufferedWriter.close();
	return outputFile;
    }
}
