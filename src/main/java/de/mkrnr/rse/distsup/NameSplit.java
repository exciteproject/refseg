package de.mkrnr.rse.distsup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameSplit {
    List<String> nameSplit;
    List<Boolean> isNameSplit;

    private final Pattern nameTagPattern = Pattern.compile("<name>(.+?)</name>");
    // matches names in xml tags including characters before and after until
    // the first space (if existing)
    // private final Pattern xmlTagPatternWithContext = Pattern
    // .compile("\\s?([^\\s]*)<([^<>]+)>([^<>]+)</([^<>]+)>([^\\s]*)\\s?");
    private final Pattern nameTagPatternWithContext = Pattern
	    .compile("\\s?([^\\s]*)<name>([^<>]+)</name>([^\\s]*)\\s?");

    public NameSplit() {
	this.nameSplit = new ArrayList<String>();
	this.isNameSplit = new ArrayList<Boolean>();
    }

    public NameSplit(String inputString) {
	Matcher matcher = this.nameTagPatternWithContext.matcher(inputString);
	int endOfLastMatchIndex = 0;

	this.nameSplit = new ArrayList<String>();
	this.isNameSplit = new ArrayList<Boolean>();

	while (matcher.find()) {
	    // get string before the match
	    if (endOfLastMatchIndex < matcher.start()) {
		this.nameSplit.add(inputString.substring(endOfLastMatchIndex, matcher.start()));
		this.isNameSplit.add(false);
	    }
	    this.nameSplit.add(inputString.substring(matcher.start(), matcher.end()));
	    this.isNameSplit.add(true);

	    endOfLastMatchIndex = matcher.end();
	}

	// add substring after last match
	if (endOfLastMatchIndex < inputString.length()) {
	    this.nameSplit.add(inputString.substring(endOfLastMatchIndex, inputString.length()));
	    this.isNameSplit.add(false);
	}
    }

    public void add(String string, boolean isName) {
	this.nameSplit.add(string);
	this.isNameSplit.add(isName);
    }

    public String get(int index) {
	return this.nameSplit.get(index);
    }

    public String getName(int index) {
	final Matcher nametagPatternMatcher = this.nameTagPattern.matcher(this.nameSplit.get(index));
	nametagPatternMatcher.find();
	return nametagPatternMatcher.group(1);
    }

    public NameSplit getSubSplit(int contextStart, int contextEnd) {
	NameSplit subSplit = new NameSplit();
	for (int i = contextStart; i <= contextEnd; i++) {
	    subSplit.add(this.get(i), this.isName(i));
	}
	return subSplit;
    }

    public boolean isName(int index) {
	return this.isNameSplit.get(index);
    }

    public String set(int index, String string) {
	return this.nameSplit.set(index, string);
    }

    public int size() {
	return this.nameSplit.size();
    }

    @Override
    public String toString() {
	return Arrays.toString(this.nameSplit.toArray(new String[0]));
    }
}
