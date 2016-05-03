package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.mkrnr.rse.util.FileHelper;

public class NameContextMatcher {

    public static void main(String[] args) throws IOException {
	File nameFile = new File(args[0]);
	File taggedFile = new File(args[1]);
	NameContextMatcher nameContextExtractor = new NameContextMatcher(nameFile);
	nameContextExtractor.extractContexts(taggedFile, 2);
    }

    // generate name lookup
    // read file
    // remove linebreaks
    // sliding window:
    // at position i, check if name at i with name at i+1 is in database (check
    // all possibilities)
    // if yes, get context and print it/store in array

    /**
     * Format of names: Map<firstName,Set<lastName>>
     */
    private Map<String, Set<String>> namesLookup;

    /**
     * Constructor that generates a lookup for the names in nameFile TODO:
     * specify format of nameFile
     *
     * @param nameFile
     * @throws IOException
     */
    public NameContextMatcher(File nameFile) throws IOException {
	this.namesLookup = this.generateNamesLookUp(nameFile);
    }

    public String[] extractContexts(File taggedFile, int contextSize) throws IOException {
	String taggedText = FileHelper.readFile(taggedFile);
	// replace linebreaks
	// TODO change replacement
	taggedText = taggedText.replaceAll(System.getProperty("line.separator"), " <lineSeparator/> ");

	List<String> contexts = new ArrayList<String>();

	// for (int i = 0 + contextSize; i < (taggedText.length - 3); i++) {
	// for (int j = i - contextSize; j < (i + 2 + contextSize); j++) {
	// System.out.println(i + " " + words[j]);
	// }
	// System.out.println("---");
	// }
	return contexts.toArray(new String[0]);
    }

    private Map<String, Set<String>> generateNamesLookUp(File nameFile) throws IOException {
	Map<String, Set<String>> namesLookup = new HashMap<String, Set<String>>();
	BufferedReader nameReader = new BufferedReader(new FileReader(nameFile));

	String line;
	while ((line = nameReader.readLine()) != null) {
	    String[] lineSplit = line.split("\t");
	    if (lineSplit.length != 3) {
		nameReader.close();
		throw new IOException("line length != 3: \"" + line + "\"");
	    }
	    String firstName = lineSplit[0];
	    String lastName = lineSplit[0];
	    if (namesLookup.containsKey(firstName)) {
		namesLookup.get(firstName).add(lastName);
	    } else {
		Set<String> lastNames = new HashSet<String>();
		lastNames.add(lastName);
		namesLookup.put(firstName, lastNames);
	    }
	}
	nameReader.close();

	return namesLookup;
    }
}
