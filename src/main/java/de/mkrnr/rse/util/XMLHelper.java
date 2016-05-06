package de.mkrnr.rse.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLHelper {

    private static final Pattern XML_TAGS_PATTERN = Pattern.compile("<([^<>]+)>([^<>]+)</([^<>]+)>");

    // matches names in xml tags including characters before and after until
    // the first space (if existing)
    private static final Pattern XML_TAGS_WITH_CONTEXT_PATTERN = Pattern
	    .compile("\\s?([^\\s]*)" + XMLHelper.XML_TAGS_PATTERN.pattern() + "([^\\s]*)\\s?");

    public static Matcher getXMLTagMatcher(String inputString) {
	return XMLHelper.XML_TAGS_PATTERN.matcher(inputString);
    }

    public static Matcher getXMLTagWithContextMatcher(String inputString) {
	return XMLHelper.XML_TAGS_WITH_CONTEXT_PATTERN.matcher(inputString);
    }

    public static String removeTags(String input) {
	String result = input;

	result = result.replaceAll("<([^<>]+)>", "");

	return result;
    }
}
