package de.exciteproject.refseg.distsup;

import java.util.HashSet;
import java.util.Set;

public class Name {

    public static Set<String> getFirstNameVariations(String firstName) {
	String[] firstNameSplit = firstName.split("\\s");
	Set<String> firstNameVariations = new HashSet<String>();

	for (String firstNameElement : firstNameSplit) {
	    if (firstNameElement.isEmpty()) {
		return firstNameVariations;
	    }
	}

	firstNameVariations.add(firstName);
	// firstNameVariations.add(Name.getFirstNDot(firstNameSplit));
	firstNameVariations.add(Name.getFirstN(firstNameSplit));
	// firstNameVariations.add(Name.getFDotSpaceNDot(firstNameSplit));
	// firstNameVariations.add(Name.getFDotSpaceN(firstNameSplit));
	// firstNameVariations.add(Name.getFDotNDot(firstNameSplit));
	firstNameVariations.add(Name.getFDotN(firstNameSplit));
	firstNameVariations.add(Name.getFN(firstNameSplit));
	return firstNameVariations;
    }

    public static void main(String[] args) {
	Set<String> firstNameVariations = Name.getFirstNameVariations("Markus Anton");
	for (String firstNameVariation : firstNameVariations) {
	    System.out.println(firstNameVariation);
	}
	System.out.println(firstNameVariations.size());

    }

    private static String getFDotN(String[] firstNames) {
	String result = "";
	for (String firstName : firstNames) {
	    result += firstName.substring(0, 1) + ".";
	}
	result = result.replaceFirst(".$", "");

	return result;
    }

    private static String getFDotNDot(String[] firstNames) {
	String result = "";
	for (String firstName : firstNames) {
	    result += firstName.substring(0, 1) + ".";
	}

	return result;
    }

    private static String getFDotSpaceN(String[] firstNames) {
	String result = "";
	for (String firstName : firstNames) {
	    result += firstName.substring(0, 1) + ". ";
	}
	result = result.replaceFirst(". $", "");

	return result;
    }

    private static String getFDotSpaceNDot(String[] firstNames) {
	String result = "";
	for (String firstName : firstNames) {
	    result += firstName.substring(0, 1) + ". ";
	}
	result = result.replaceFirst(" $", "");

	return result;
    }

    private static String getFirstN(String[] firstNames) {
	String result = "";
	result += firstNames[0];
	if (firstNames.length > 1) {
	    for (int i = 1; i < firstNames.length; i++) {
		result += " " + firstNames[i].substring(0, 1);
	    }
	}
	return result;
    }

    private static String getFirstNDot(String[] firstNames) {
	String result = "";

	result += firstNames[0];
	if (firstNames.length > 1) {
	    for (int i = 1; i < firstNames.length; i++) {
		result += " " + firstNames[i].substring(0, 1) + ".";
	    }
	}
	return result;

    }

    private static String getFN(String[] firstNames) {
	String result = "";
	for (String firstName : firstNames) {
	    result += firstName.substring(0, 1);
	}
	return result;
    }

}
