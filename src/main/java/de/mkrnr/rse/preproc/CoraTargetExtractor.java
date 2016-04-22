package de.mkrnr.rse.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

import cc.mallet.util.CharSequenceLexer;

/**
 * A class for labeling tokens based on the XML tags that they appear in.
 */
public class CoraTargetExtractor {

    public static void main(String[] args) {
	Pattern tokenPattern = Pattern.compile("\\S+");

	String[] relevantTags = new String[1];
	relevantTags[0] = "author";
	CoraTargetExtractor xmlTargetExtractor = new CoraTargetExtractor(tokenPattern, relevantTags, "other");
	xmlTargetExtractor.extractTargets(new File(args[0]), new File(args[1]));
	// xmlTargetExtractor.extractTargetsInDir(new File(args[0]), new
	// File(args[1]));
    }

    private CharSequenceLexer lexer;
    private HashSet<String> relevantTags;
    private String untaggedLabel;
    private boolean filterTags;

    public CoraTargetExtractor(Pattern regex) {
	this.lexer = new CharSequenceLexer(regex);
	this.filterTags = false;
    }

    public CoraTargetExtractor(Pattern regex, String[] relevantTags, String untaggedLabel) {
	this.lexer = new CharSequenceLexer(regex);

	this.relevantTags = new HashSet<String>();
	for (String relevantTag : relevantTags) {
	    this.relevantTags.add(relevantTag);
	}
	this.untaggedLabel = untaggedLabel;
	this.filterTags = true;
    }

    public void extractTargets(File inputFile, File outputFile) {
	System.out.println(inputFile.getAbsolutePath());
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
			if ((currentTag == null) || (this.filterTags && !this.relevantTags.contains(currentTag))) {
			    bufferedWriter.write(tokenString + " " + this.untaggedLabel + "\n");

			} else {
			    bufferedWriter.write(tokenString + " " + currentTag + "\n");
			}
		    }
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

    public void extractTargetsInDir(File inputDir, File outputDir) throws FileNotFoundException {
	outputDir.mkdirs();

	try {
	    for (File inputFile : inputDir.listFiles()) {
		this.extractTargets(inputFile,
			new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
	    }
	} catch (NullPointerException e) {
	    throw new FileNotFoundException();
	}
    }
}
