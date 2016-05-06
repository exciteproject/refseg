package de.mkrnr.rse.distsup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import de.mkrnr.rse.util.XMLHelper;

public class NameSplit {

    // public static void main(String[] args) {
    // NameSplit nameSplit = new NameSplit(
    // "<lastName>Sassen</lastName>, <firstName>Saskia</firstName> (1991),
    // <lastName>The</lastName> Global "
    // + "City. <lastName>New</lastName> <lastName>York</lastName>,
    // <lastName>London</lastName>, Tokyo,");
    // System.out.println(nameSplit.toString());
    // }

    List<String> nameSplit;
    List<String> nameSplitTag;

    public NameSplit() {
	this.nameSplit = new ArrayList<String>();
	this.nameSplitTag = new ArrayList<String>();
    }

    public NameSplit(String inputString) {
	final Matcher matcher = XMLHelper.getXMLTagWithContextMatcher(inputString);
	int endOfLastMatchIndex = 0;

	this.nameSplit = new ArrayList<String>();
	this.nameSplitTag = new ArrayList<String>();

	while (matcher.find()) {
	    // get string before the match
	    if (endOfLastMatchIndex < matcher.start()) {
		this.nameSplit.add(inputString.substring(endOfLastMatchIndex, matcher.start()));
		this.nameSplitTag.add(null);
	    }
	    this.nameSplit.add(inputString.substring(matcher.start(), matcher.end()));
	    this.nameSplitTag.add(matcher.group(2));

	    endOfLastMatchIndex = matcher.end();
	}
	// add substring after last match
	if (endOfLastMatchIndex < inputString.length()) {
	    this.nameSplit.add(inputString.substring(endOfLastMatchIndex, inputString.length()));
	    this.nameSplitTag.add(null);
	}
    }

    public void add(String string, String tag) {
	this.nameSplit.add(string);
	this.nameSplitTag.add(tag);
    }

    public void addTag(int startNameIndex, int endNameIndex, String authorTag) {
	String startNameWithTag = this.get(startNameIndex);
	String endNameWithTag = this.get(endNameIndex);
	this.remove(endNameIndex);
	this.remove(startNameIndex);
	String taggedAuthorName = startNameWithTag + endNameWithTag;
	taggedAuthorName = this.addStringAfterLastNonSpace(taggedAuthorName, "</" + authorTag + ">");
	taggedAuthorName = this.addStringBeforeFirstNonSpace(taggedAuthorName, "<" + authorTag + ">");
	this.add(startNameIndex, taggedAuthorName, "<" + authorTag + ">");
    }

    public void formatTaggedName(int index, String nameTag) {
	this.removeNameTag(index);

	String formattedTaggedName = this.get(index);
	formattedTaggedName = this.addStringBeforeFirstNonSpace(formattedTaggedName, "<" + nameTag + ">");
	formattedTaggedName = this.addStringAfterLastNonSpace(formattedTaggedName, "</" + nameTag + ">");

	this.nameSplit.set(index, formattedTaggedName);
    }

    public String get(int index) {
	return this.nameSplit.get(index);
    }

    public String getName(int index) {
	final Matcher nametagPatternMatcher = XMLHelper.getXMLTagMatcher(this.nameSplit.get(index));
	nametagPatternMatcher.find();
	return nametagPatternMatcher.group(2);
    }

    public String getTag(int index) {
	return this.nameSplitTag.get(index);
    }

    public void remove(int index) {
	this.nameSplit.remove(index);
	this.nameSplitTag.remove(index);
    }

    public void removeNameTag(int index) {
	String nameWithRemovedTag = XMLHelper.removeTags(this.get(index));

	this.set(index, nameWithRemovedTag, null);
    }

    public int size() {
	return this.nameSplit.size();
    }

    public String toDebugString() {
	String result = "";
	for (String nameSplitPart : this.nameSplit) {
	    result += "\"" + nameSplitPart + "\"\n";
	}
	return result;
    }

    @Override
    public String toString() {
	return String.join("", this.nameSplit);
    }

    private void add(int startNameIndex, String string, String tag) {
	this.nameSplit.add(startNameIndex, string);
	this.nameSplitTag.add(startNameIndex, tag);

    }

    private String addStringAfterLastNonSpace(String name, String stringToAdd) {
	String result = name;
	for (int i = result.length() - 1; i >= 0; i--) {
	    if (!Character.isWhitespace(result.charAt(i))) {

		String substringAfterTag = "";
		if ((i + 1) < result.length()) {
		    substringAfterTag = result.substring(i + 1);
		}

		result = result.substring(0, i + 1) + stringToAdd + substringAfterTag;
		break;
	    }
	}
	return result;
    }

    private String addStringBeforeFirstNonSpace(String name, String stringToAdd) {
	String result = name;
	for (int i = 0; i < result.length(); i++) {
	    if (!Character.isWhitespace(result.charAt(i))) {
		result = result.substring(0, i) + stringToAdd + result.substring(i);
		break;
	    }
	}
	return result;
    }

    private void set(int index, String result, String tag) {
	this.nameSplit.set(index, result);
	this.nameSplitTag.set(index, tag);
    }
}
