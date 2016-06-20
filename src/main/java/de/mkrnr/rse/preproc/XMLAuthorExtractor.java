package de.mkrnr.rse.preproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import cc.mallet.util.CharSequenceLexer;

/**
 * A class for labeling tokens based on the XML tags that they appear in.
 */
public class XMLAuthorExtractor {

    public static void main(String[] args) throws FileNotFoundException {
	Pattern tokenPattern = Pattern.compile("\\S+");

	Map<String, String> mappings = new HashMap<String, String>();
	mappings.put("firstName", "FN");
	mappings.put("firstAndMiddleName", "FN");
	mappings.put("middleName", "FN");
	mappings.put("lastName", "LN");
	mappings.put("prefix", "LN");
	mappings.put("other", "O");

	String authorTag = "author";
	String otherLabel = "O";

	XMLAuthorExtractor xmlTargetExtractor = new XMLAuthorExtractor(mappings, tokenPattern, otherLabel, false);
	// xmlTargetExtractor.extractAuthors(new File(args[0]), new
	// File(args[1]));
	xmlTargetExtractor.extractAuthorsInDir(authorTag, new File(args[0]), new File(args[1]));
    }

    private CharSequenceLexer lexer;
    private String untaggedLabel;
    private boolean setLastMark;
    private Map<String, String> mappings;

    /**
     *
     * @param mappings
     * @param regex:
     *            used by a lexer to split the strings into tokens
     * @param untaggedLabel:
     *            label that is assigned when a string is not surrounded by tag
     * @param setLastMark:
     *            if true: marks the last author element as such
     */
    public XMLAuthorExtractor(Map<String, String> mappings, Pattern regex, String untaggedLabel, boolean setLastMark) {
	this.mappings = mappings;
	this.lexer = new CharSequenceLexer(regex);
	this.untaggedLabel = untaggedLabel;
	this.setLastMark = setLastMark;
    }

    public void extractAuthors(String authorTag, File inputFile, File outputFile) {
	System.out.println(inputFile.getAbsolutePath());
	try {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

	    boolean authorTagOpened = false;
	    String authorContent = "";
	    String line;
	    while (true) {
		line = bufferedReader.readLine();

		if (line == null) {
		    bufferedReader.close();
		    bufferedWriter.close();
		    if (authorTagOpened) {
			throw new IllegalStateException(
				"finished reading " + inputFile.getName() + " while scanning for closing author tag");
		    }
		    break;
		}
		// preprocess line
		line = this.preprocessLine(line);

		this.lexer.setCharSequence(line);
		while (this.lexer.hasNext()) {
		    this.lexer.next();

		    String tokenString = this.lexer.getTokenString();
		    // match tags
		    if (tokenString.matches("<.*>")) {
			// match end tag
			if (tokenString.matches("</.*>")) {
			    // check if closing tag matches the author tag
			    if (authorTag.equals(tokenString.substring(2, tokenString.length() - 1))) {
				this.extractAuthorContent(authorContent, bufferedWriter);
				authorContent = "";
				authorTagOpened = false;
			    } else {
				authorContent += " " + tokenString;
			    }
			} else {
			    if (authorTag.equals(tokenString.substring(1, tokenString.length() - 1))) {
				authorTagOpened = true;
			    } else {
				authorContent += " " + tokenString;
			    }
			}
		    } else {
			if (authorTagOpened) {
			    authorContent += " " + tokenString;
			} else {
			    this.writeTokenAndLabel(tokenString, this.untaggedLabel, bufferedWriter);
			}
		    }
		}
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("inputFile was not found: " + inputFile.getAbsolutePath());
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void extractAuthorsInDir(String authorTag, File inputDir, File outputDir) throws FileNotFoundException {
	outputDir.mkdirs();
	try {
	    for (File inputFile : inputDir.listFiles()) {
		this.extractAuthors(authorTag, inputFile,
			new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
	    }
	} catch (NullPointerException e) {
	    throw new FileNotFoundException();
	}
    }

    private void extractAuthorContent(String authorContent, BufferedWriter bufferedWriter) {
	CharSequenceLexer localLexer = new CharSequenceLexer(this.lexer.getPattern());
	localLexer.setCharSequence(authorContent);

	ArrayList<String> authorTokenStrings = new ArrayList<String>();
	ArrayList<String> authorTags = new ArrayList<String>();
	String currentTag = null;
	while (localLexer.hasNext()) {
	    localLexer.next();

	    String tokenString = localLexer.getTokenString();
	    // match tags
	    if (tokenString.matches("<.*>")) {
		// match end tag
		if (tokenString.matches("</.*>")) {
		    // check if closing tag matches the current tag
		    try {
			if (currentTag.equals(tokenString.substring(2, tokenString.length() - 1))) {
			    currentTag = null;
			} else {
			    throw new IllegalStateException("XML is not well formatted: " + authorContent);
			}
		    } catch (NullPointerException e) {
			throw new NullPointerException("empty element in author content: " + authorContent);
		    }
		} else {
		    currentTag = tokenString.substring(1, tokenString.length() - 1);
		}
	    } else {
		authorTokenStrings.add(tokenString);
		if ((currentTag == null)) {
		    authorTags.add(this.untaggedLabel);
		} else {
		    authorTags.add(currentTag);
		}
	    }
	}

	for (int i = 0; i < authorTokenStrings.size(); i++) {
	    String prefix = "";
	    if (i == 0) {
		prefix = "B-";
	    } else {
		if ((i == (authorTokenStrings.size() - 1)) && this.setLastMark) {
		    prefix = "E-";
		} else {
		    prefix = "I-";
		}
	    }
	    String tagLabel = "";
	    if (this.mappings.containsKey(authorTags.get(i))) {
		tagLabel = this.mappings.get(authorTags.get(i));
	    } else {
		System.err.println("Author tag not found: " + authorTags.get(i));
		System.out.println(authorTokenStrings.get(i));
		System.exit(1);
	    }
	    this.writeTokenAndLabel(authorTokenStrings.get(i), prefix + tagLabel, bufferedWriter);

	}
    }

    private String preprocessLine(String line) {
	line = line.replaceAll("<", " <");
	line = line.replaceAll(">", "> ");
	return line;
    }

    private void writeTokenAndLabel(String tokenString, String label, BufferedWriter bufferedWriter) {
	try {
	    bufferedWriter.write(tokenString + " " + label + "\n");
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
}
