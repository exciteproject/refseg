package de.mkrnr.rse.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class for extracting reference strings from the ouput files of the Cora
 * Research Paper Classification data available at
 * https://people.cs.umass.edu/~mccallum/data.html
 */
public class CoraReferenceExtractor {

    public static void main(String[] args) {

        CoraReferenceExtractor coraReferenceExtractor = new CoraReferenceExtractor();

        // coraReferenceExtractor.extractReferencesInDir(new
        // File("/home/martin/downloads/cora-classify/cora/extractions"),
        // //
        // "/home/martin/downloads/cora-classify/cora/extractions/file:##cse.ogi.edu#pub#ogipvm#papers#CLO.ps.gz"),
        // new File("/home/martin/tmp/cora/annotated-references"));
        coraReferenceExtractor.removeXMLTagsInDir(new File("/home/martin/tmp/cora/annotated-references"),
                new File("/home/martin/tmp/cora/references"));
        // coraReferenceExtractor.extractReferences(new File(
        // "/home/martin/downloads/cora-classify/cora/extractions/file:##cdr.stanford.edu#pub#CDR#Publications#Reports#design-nav.ps"),
        // //
        // "/home/martin/downloads/cora-classify/cora/extractions/file:##cse.ogi.edu#pub#ogipvm#papers#CLO.ps.gz"),
        // new File("/home/martin/tmp/cora-test-refs.txt"));
        // coraReferenceExtractor.removeXMLTags(new
        // File("/home/martin/tmp/cora-test-refs.txt"),
        // new File("/home/martin/tmp/cora-test-refs-clean.txt"));
    }

    public void extractReferences(File inputFile, File outputFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("Reference: ")) {
                    line = line.replace("Reference: ", "");
                    bufferedWriter.write(line + System.lineSeparator());
                }
            }
            bufferedWriter.close();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("inputFile was not found: " + inputFile.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void extractReferencesInDir(File inputDir, File outputDir) {
        outputDir.mkdirs();
        for (File inputFile : inputDir.listFiles()) {
            this.extractReferences(inputFile,
                    new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
        }
    }

    public void removeXMLTags(File inputFile, File outputFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                // System.out.println("before:\t|" + line + "|");

                // remove first XML tag
                line = line.replaceAll("^<[^>]+>\\s", "");

                // remove XML tags, possible multiple in a row
                line = line.replaceAll("\\s(<[^>]+>\\s)+", " ");

                // remove last XML tag
                line = line.replaceAll("\\s<[^>]+>", "");

                // System.out.println("after:\t|" + line + "|");
                bufferedWriter.write(line + System.lineSeparator());
            }
            bufferedWriter.close();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("inputFile was not found: " + inputFile.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeXMLTagsInDir(File inputDir, File outputDir) {
        outputDir.mkdirs();
        for (File inputFile : inputDir.listFiles()) {
            this.removeXMLTags(inputFile, new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
        }
    }

}
