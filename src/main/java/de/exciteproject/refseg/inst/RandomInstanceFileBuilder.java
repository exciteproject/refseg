package de.exciteproject.refseg.inst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    public static File createInstanceFile(List<File> inputFiles, File outputFile, List<String> labels)
	    throws IOException {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

	for (File inputFile : inputFiles) {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		String[] lineSplit = line.split("\\s+");
		for (String word : lineSplit) {
		    if (!word.isEmpty()) {
			int labelIndex = (int) (Math.random() * labels.size());
			bufferedWriter.write(word + " " + labels.get(labelIndex) + System.lineSeparator());
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
