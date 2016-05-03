package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mkrnr.rse.util.FileHelper;

public class NameContextExtractor {

    public static void main(String[] args) throws IOException {
	File nameFile = new File(args[0]);
	File taggedFile = new File(args[1]);
	NameContextExtractor nameContextExtractor = new NameContextExtractor(nameFile);
	nameContextExtractor.extractContexts(taggedFile, 2);

    }

    // generate name lookup
    // read file
    // remove linebreaks
    // sliding window:
    // at position i, check if name at i with name at i+1 is in database (check
    // all possibilities)
    // if yes, get context and print it/store in array

    private Map<String, boolean[]> nameTypeMap;

    private final Pattern nameTagPattern = Pattern.compile("<name>(.+?)</name>");
    // matches names in xml tags including characters before and after until
    // the first space (if existing)
    private final Pattern xmlTagPatternWithContext = Pattern
	    .compile("\\s?([^\\s]*)<([^<>]+)>([^<>]+)</([^<>]+)>([^\\s]*)\\s?");

    /**
     * Constructor that generates a lookup for the names in nameFile TODO:
     * specify format of nameFile
     *
     * @param nameFile
     * @throws IOException
     */
    public NameContextExtractor(File nameFile) throws IOException {
	this.nameTypeMap = this.generateNameMap(nameFile);
    }

    public String[] extractContexts(File taggedFile, int contextSize) throws IOException {
	String taggedText = FileHelper.readFile(taggedFile);

	// replace linebreaks
	// TODO change replacement
	taggedText = taggedText.replaceAll(System.getProperty("line.separator"), " <lineSeparator/> ");

	List<String> contexts = new ArrayList<String>();

	// TODO remove
	String test = "Test von <name>Dietze</name>. <name>C. A.</name>; <name>Rolfes</name>, <name>M.</name> täst";

	Matcher matcher = this.xmlTagPatternWithContext.matcher(test);
	int endOfLastMatchIndex = 0;

	List<String> nameSplits = new ArrayList<String>();
	List<Boolean> ifNameSplits = new ArrayList<Boolean>();

	while (matcher.find()) {
	    // get string before the match
	    if (endOfLastMatchIndex < matcher.start()) {
		nameSplits.add(test.substring(endOfLastMatchIndex, matcher.start()));
		ifNameSplits.add(false);
	    }
	    nameSplits.add(test.substring(matcher.start(), matcher.end()));
	    ifNameSplits.add(true);

	    endOfLastMatchIndex = matcher.end();
	}
	// add substring after last match
	if (endOfLastMatchIndex < test.length()) {
	    nameSplits.add(test.substring(endOfLastMatchIndex, test.length()));
	    ifNameSplits.add(false);
	}

	// nameSplits.size()-1 because of name check at position i + 1
	for (int i = 0; i < (nameSplits.size() - 1); i++) {
	    if (ifNameSplits.get(i) && ifNameSplits.get(i + 1)) {
		String iName = this.getName(nameSplits.get(i));
		String iPlusOneName = this.getName(nameSplits.get(i + 1));

		// TODO write out all pairs of possible names (first+last or
		// last+first)

	    }
	}
	return contexts.toArray(new String[0]);
    }

    private Map<String, boolean[]> generateNameMap(File nameFile) throws IOException {
	Map<String, boolean[]> nameTypeMap = new HashMap<String, boolean[]>();

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
	    if (nameTypeMap.containsKey(firstName)) {
		nameTypeMap.get(firstName)[0] = true;
	    } else {
		boolean[] nameTypes = { true, false };
		nameTypeMap.put(firstName, nameTypes);
	    }
	    if (nameTypeMap.containsKey(lastName)) {
		nameTypeMap.get(lastName)[1] = true;
	    } else {
		boolean[] nameTypes = { false, true };
		nameTypeMap.put(firstName, nameTypes);
	    }
	}
	nameReader.close();

	return nameTypeMap;
    }

    private String getName(String taggedName) {
	final Matcher nametagPatternMatcher = this.nameTagPattern.matcher(taggedName);
	nametagPatternMatcher.find();
	return nametagPatternMatcher.group(1);
    }
}
