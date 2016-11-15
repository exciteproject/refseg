package de.exciteproject.refseg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Class for counting unique lines in a file.
 */
public class CountUtils {

    public static void countUniqueLines(File inputFile, File outputFile) throws IOException {
        Map<String, Integer> countMap = new TreeMap<String, Integer>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            MapUtils.addCount(countMap, line);
        }
        bufferedReader.close();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (Entry<String, Integer> countMapEntry : countMap.entrySet()) {
            bufferedWriter.write(countMapEntry.getKey() + '\t' + countMapEntry.getValue());
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    public static void main(String[] args) throws IOException {
        File inputDirectory = new File(args[0]);
        File outputDirectory = new File(args[1]);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        for (File inputFile : inputDirectory.listFiles()) {
            CountUtils.countUniqueLines(inputFile,
                    new File(outputDirectory.getAbsolutePath() + "/" + inputFile.getName()));
        }
    }
}
