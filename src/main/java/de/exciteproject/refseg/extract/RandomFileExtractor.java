package de.exciteproject.refseg.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.exciteproject.refseg.util.FileUtils;

/**
 * Class for randomly selecting files in a given directory (including sub
 * directories)
 */
public class RandomFileExtractor {

    public static void main(String[] args) throws IOException {
        File inputDirectory = new File(args[0]);
        File outputDirectory = new File(args[1]);

        int numberOfFiles = Integer.parseInt(args[2]);
        List<File> extractedFiles = RandomFileExtractor.randomlySelectFiles(inputDirectory, numberOfFiles,
                new ArrayList<File>());
        FileUtils.copyFiles(extractedFiles, inputDirectory, outputDirectory);

    }

    public static List<File> randomlySelectFiles(File inputDirectory, int numberOfFiles, List<File> filesToExclude)
            throws IOException {
        List<File> inputFiles = FileUtils.listFilesRecursively(inputDirectory);
        Collections.shuffle(inputFiles);

        List<File> selectedFiles = new ArrayList<File>();
        for (File inputFile : inputFiles) {
            if (selectedFiles.size() == numberOfFiles) {
                continue;
            }
            if (!filesToExclude.contains(inputFile)) {
                selectedFiles.add(inputFile);
            }
        }
        return selectedFiles;
    }
}
