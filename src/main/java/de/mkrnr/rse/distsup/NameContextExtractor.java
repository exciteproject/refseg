package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final String firstNameTagLabel = "firstName";
    private final String lastNameTagLabel = "lastName";

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

	// TODO remove
	String test = "<name>Test</name> test Test von <name>Michael</name>. <name>Friedrich</name>; <name>Michael</name>, <name>Friedrich</name> täst";

	NameSplit nameSplit = new NameSplit(test);

	// for (int i = 0; i < nameSplits.size(); i++) {
	// System.out.println(nameSplits.get(i));
	// }

	// nameSplits.size()-1 because of name check at position i + 1
	List<NameSplit> contexts = this.extractContexts(nameSplit, contextSize);

	return contexts.toArray(new String[0]);
    }

    private List<NameSplit> extractContexts(NameSplit nameSplit, int contextSize) {
	List<NameSplit> contexts = new ArrayList<NameSplit>();
	for (int i = 0; i < (nameSplit.size() - 1); i++) {
	    if (nameSplit.isName(i) && nameSplit.isName(i + 1)) {
		String iName = nameSplit.getName(i);
		String iPlusOneName = nameSplit.getName(i + 1);

		boolean[] iNameTypes = this.nameTypeMap.get(iName);
		boolean[] iPlusOneNameTypes = this.nameTypeMap.get(iPlusOneName);

		// iName or IPlusOneName not found
		if ((iNameTypes == null) || (iPlusOneNameTypes == null)) {
		    continue;
		}

		// iName = first name && iPlusOneName = last name
		if (iNameTypes[0] && iPlusOneNameTypes[1]) {
		    // TODO add recursion
		    contexts.addAll(this.getTaggedContext(nameSplit, i, this.firstNameTagLabel, this.lastNameTagLabel,
			    contextSize));
		}
		if (iNameTypes[1] && iPlusOneNameTypes[0]) {
		    // TODO add recursion
		    contexts.addAll(this.getTaggedContext(nameSplit, i, this.lastNameTagLabel, this.firstNameTagLabel,
			    contextSize));
		}
	    }
	}

	return contexts;
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

    private List<NameSplit> getTaggedContext(NameSplit nameSplit, int indexInNameSplits, String iNameTagLabel,
	    String iPlusOneNameTagLabel, int contextSize) {
	List<NameSplit> contexts = new ArrayList<NameSplit>();

	int contextStart = indexInNameSplits - contextSize;
	if (contextStart < 0) {
	    contextStart = 0;
	}

	int contextEnd = indexInNameSplits + 1 + contextSize;
	if (contextEnd > (nameSplit.size() - 1)) {
	    contextEnd = nameSplit.size() - 1;
	}

	NameSplit context = nameSplit.getSubSplit(contextStart, contextEnd);

	int iNameIndex = contextSize;
	int iPlusOneNameIndex = contextSize + 1;
	context.set(iNameIndex, this.setTags(context.get(iNameIndex), iNameTagLabel));
	context.set(iPlusOneNameIndex, this.setTags(context.get(iPlusOneNameIndex), iPlusOneNameTagLabel));

	System.out.println(context.toString());

	return contexts;
    }

    private String setTags(String nameWithTags, String tagLabel) {
	String nameWithChangedTags = nameWithTags;
	nameWithChangedTags = nameWithChangedTags.replaceFirst("<([^<>/]+)>", "<" + tagLabel + ">");
	nameWithChangedTags = nameWithChangedTags.replaceFirst("</([^<>]+)>", "</" + tagLabel + ">");
	return nameWithChangedTags;
    }
}
