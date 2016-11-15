package de.exciteproject.refseg.inst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.exciteproject.refseg.distsup.ReferenceNormalizer;

/**
 * Class for generating instance files by segmenting input strings. Predefined
 * labels are assigned iteratively and independent from the actual string. This
 * "workaround" is recommended in the Mallet documentation when applying
 * generalized expectation during the training.
 */
public class TrainingInstanceFileBuilder {

    /**
     * Creates an instance file by iteratively picking from the passed labels
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
            int lineIndex = 0;
            while ((line = bufferedReader.readLine()) != null) {
                line = ReferenceNormalizer.splitAfterPunctuation(line);
                String[] lineSplit = line.split("\\s+");
                for (String word : lineSplit) {
                    if (!word.isEmpty()) {
                        int labelIndex = lineIndex % labels.size();
                        lineIndex++;
                        bufferedWriter.write(word + " " + labels.get(labelIndex) + System.lineSeparator());
                    }
                }
                bufferedWriter.newLine();
            }
            bufferedReader.close();
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
