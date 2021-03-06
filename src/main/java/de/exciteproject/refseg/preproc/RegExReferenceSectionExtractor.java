package de.exciteproject.refseg.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import de.exciteproject.refseg.util.FileUtils;

/**
 * Class for extracting reference sections using a simple matching approach with
 * regular expressions searching for Section headings.
 */
public class RegExReferenceSectionExtractor extends Extractor {

    private static final String REFERENCE_HEADER_REGEX = "[^\\p{IsAlphabetic}]*(Bibliography|"
            + "BIBLIOGRAPHY|Bibliographie|BIBLIOGRAPHIE|Referenzen|REFERENZEN|References|REFERENCES|Reference List|REFERENCE LIST|Literatur|LITERATUR|Sources|SOURCES|Schrifttum).*";

    private static final String AFTER_REFERENCE_HEADER_REGEX = "[^\\p{IsAlphabetic}]*(Tabellenanhang|ANNEXES|Abstract|ABSTRACT|Sonstige|Sonstiges|Notes|NOTES|Anmerkungen|ANMERKUNGEN|Appendix|APPENDIX|"
            + "ANHANG|Anhang|Author|AUTHOR|Autor|AUTOR|Bemerkungen|Die Schriftenreihe|Discussion|DISCUSSION|Eingereicht am|Forschungsschwerpunkt|Stiftungsmaterialien|Table|TABLE|Zur Person|Zusammenfassung).*";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        RegExReferenceSectionExtractor regExReferenceSectionExtractor = new RegExReferenceSectionExtractor();
        // regExReferenceSectionExtractor.extract(new File(args[0] +
        // "/10454.txt"), null);
        regExReferenceSectionExtractor.extractInDir(new File(args[0]), new File(args[1]));
        long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("This took " + (endTime - startTime) + " milliseconds");
    }

    private int maxLines = 2000;

    @Override
    public void extract(File inputFile, File outputFile) {
        try {
            String input = FileUtils.readFile(inputFile);
            int totalLines = input.split(System.getProperty("line.separator")).length;
            BufferedReader bufferedReader = new BufferedReader(new StringReader(input));
            String line = null;
            StringBuffer referenceSection = new StringBuffer();
            boolean referenceSectionFound = false;
            boolean afterReferenceSectionFound = false;
            int lineCount = 0;
            int referenceSectionLineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                // System.out.println(line);
                if (!afterReferenceSectionFound) {
                    lineCount++;
                    if (referenceSectionFound) {
                        if (Pattern.matches(AFTER_REFERENCE_HEADER_REGEX, line)) {
                            afterReferenceSectionFound = true;
                            continue;
                        }

                        if (Pattern.matches(REFERENCE_HEADER_REGEX, line)) {
                            afterReferenceSectionFound = true;
                            referenceSectionFound = false;
                            break;
                        }
                        referenceSection.append(line + System.getProperty("line.separator"));
                        referenceSectionLineCount++;

                        continue;
                    }

                    if (Pattern.matches(REFERENCE_HEADER_REGEX, line) && (lineCount > (0.7 * totalLines))) {
                        referenceSectionFound = true;
                    }
                }
            }
            // TODO check parscit paper for ratio
            if (referenceSectionFound && (referenceSectionLineCount <= this.maxLines)) {
                // if (referenceSectionFound) {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
                bufferedWriter.write(referenceSection.toString());
                bufferedWriter.close();
            }
            bufferedReader.close();
        } catch (

        IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
