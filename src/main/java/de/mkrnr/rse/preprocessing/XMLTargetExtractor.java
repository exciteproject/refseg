package de.mkrnr.rse.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import cc.mallet.util.CharSequenceLexer;

/**
 * A class for labeling tokens based on the XML tags that they appear in.
 */
public class XMLTargetExtractor {

    public static void main(String[] args) {
        Pattern tokenPattern = Pattern.compile("\\S+");

        XMLTargetExtractor xmlTargetExtractor = new XMLTargetExtractor(tokenPattern);
        xmlTargetExtractor.extractTargets(new File(args[0]), new File(args[1]));
    }

    CharSequenceLexer lexer;

    public XMLTargetExtractor(Pattern regex) {
        this.lexer = new CharSequenceLexer(regex);
    }

    public void extractTargets(File inputFile, File outputFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                this.lexer.setCharSequence(line);
                String currentTag = null;
                while (this.lexer.hasNext()) {
                    this.lexer.next();

                    String tokenString = this.lexer.getTokenString();
                    // match tags
                    if (tokenString.matches("<.*>")) {
                        // match end tag
                        if (tokenString.matches("</.*>")) {
                            // check if closing tag matches the current tag
                            if (currentTag.equals(tokenString.substring(2, tokenString.length() - 1))) {
                                currentTag = null;
                            } else {
                                bufferedWriter.close();
                                bufferedReader.close();
                                throw new IllegalStateException("XML is not well formatted: " + line);
                            }
                        } else {
                            currentTag = tokenString.substring(1, tokenString.length() - 1);
                        }
                    } else {
                        if (currentTag == null) {
                            bufferedWriter.write(tokenString + " " + "UNDEFINED" + "\n");
                        } else {
                            bufferedWriter.write(tokenString + " " + currentTag + "\n");
                        }
                    }
                }
                System.out.println();
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
}
