package de.mkrnr.rse.distsup;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Name {
    public static void main(String[] args) {
	String[] firstNames = { "Markus", "Anton" };
	String[] lastNames = { "van", "Müller" };
	Name name = new Name(firstNames, lastNames);
	String[] variations = name.getVariations(true);
	for (String variation : variations) {
	    System.out.println(variation);
	}
	System.out.println(variations.length);
    }

    private final String FIRST_NAME_TAG = "firstName";
    private final String LAST_NAME_TAG = "lastName";

    private final String AUTHOR_TAG = "author";

    private String[] firstNames;
    private String[] lastNames;

    public Name(String[] firstNames, String[] lastNames) {
	this.firstNames = firstNames;
	this.lastNames = lastNames;
    }

    public String[] getVariations(boolean withXML) {

	Set<String> variations = new LinkedHashSet<String>();

	if ((this.firstNames.length == 0) || (this.lastNames.length == 0)) {
	    return variations.toArray(new String[0]);
	}

	String lastNames = this.getLastNames(withXML);
	String lastNamesWithComma = this.getLastNamesWithComma(withXML);

	String firstNames = this.getFirstNames(withXML);
	String firstNDot = this.getFirstNDot(withXML);
	String firstN = this.getFirstN(withXML);
	String firstName = this.getFirstName(withXML);
	String fDotSpaceNDot = this.getFDotSpaceNDot(withXML);
	String fDotNDot = this.getFDotNDot(withXML);
	String fN = this.getFN(withXML);

	variations.add(firstNames + " " + lastNames);
	variations.add(firstNDot + " " + lastNames);
	variations.add(firstN + " " + lastNames);
	variations.add(firstName + " " + lastNames);
	variations.add(fDotSpaceNDot + " " + lastNames);
	variations.add(fDotNDot + " " + lastNames);
	variations.add(fN + " " + lastNames);

	variations.add(lastNamesWithComma + " " + firstNames);
	variations.add(lastNamesWithComma + " " + firstNDot);
	variations.add(lastNamesWithComma + " " + firstN);
	variations.add(lastNamesWithComma + " " + firstName);
	variations.add(lastNamesWithComma + " " + fDotSpaceNDot);
	variations.add(lastNamesWithComma + " " + fDotNDot);
	variations.add(lastNamesWithComma + " " + fN);

	Set<String> variationsWithAuthorTag = new LinkedHashSet<String>();
	if (withXML) {
	    for (String variation : variations) {
		variationsWithAuthorTag.add(this.addAuthorTags(variation));
	    }
	    return variationsWithAuthorTag.toArray(new String[0]);
	} else {
	    return variations.toArray(new String[0]);
	}

    }

    public Map<String, String> getVariationsXMLMap() {
	Map<String, String> variationsXMLMap = new HashMap<String, String>();
	String[] variations = this.getVariations(false);
	String[] variationsWithXML = this.getVariations(true);
	for (int i = 0; i < variations.length; i++) {
	    variationsXMLMap.put(variations[i], variationsWithXML[i]);
	}
	return variationsXMLMap;
    }

    private String addAuthorTags(String name) {
	name = "<" + this.AUTHOR_TAG + ">" + name + "</" + this.AUTHOR_TAG + ">";
	return name;
    }

    private String addFirstNameTags(String name, boolean withXML) {
	if (withXML) {
	    name = "<" + this.FIRST_NAME_TAG + ">" + name + "</" + this.FIRST_NAME_TAG + ">";
	}
	return name;
    }

    private String addLastNameTags(String name, boolean withXML) {
	if (withXML) {
	    name = "<" + this.LAST_NAME_TAG + ">" + name + "</" + this.LAST_NAME_TAG + ">";
	}
	return name;
    }

    private String getFDotNDot(boolean withXML) {
	String result = "";
	for (String firstName : this.firstNames) {
	    result += firstName.substring(0, 1) + ".";
	}

	result = this.addFirstNameTags(result, withXML);
	return result;
    }

    private String getFDotSpaceNDot(boolean withXML) {
	String result = "";
	for (String firstName : this.firstNames) {
	    result += firstName.substring(0, 1) + ". ";
	}
	result = result.replaceFirst(" $", "");

	result = this.addFirstNameTags(result, withXML);
	return result;
    }

    private String getFirstN(boolean withXML) {
	String result = "";
	result += this.firstNames[0];
	if (this.firstNames.length > 1) {
	    for (int i = 1; i < this.firstNames.length; i++) {
		result += " " + this.firstNames[i].substring(0, 1);
	    }
	}
	result = this.addFirstNameTags(result, withXML);
	return result;
    }

    private String getFirstName(boolean withXML) {
	String result = "";
	result += this.firstNames[0];

	result = this.addFirstNameTags(result, withXML);
	return result;
    }

    private String getFirstNames(boolean withXML) {
	String result = "";
	for (String firstNames : this.firstNames) {
	    result += firstNames + " ";
	}
	result = result.replaceFirst(" $", "");

	result = this.addFirstNameTags(result, withXML);
	return result;
    }

    private String getFirstNDot(boolean withXML) {
	String result = "";
	result += this.firstNames[0];
	if (this.firstNames.length > 1) {
	    for (int i = 1; i < this.firstNames.length; i++) {
		result += " " + this.firstNames[i].substring(0, 1) + ".";
	    }
	}
	result = this.addFirstNameTags(result, withXML);

	return result;
    }

    private String getFN(boolean withXML) {
	String result = "";
	for (String firstName : this.firstNames) {
	    result += firstName.substring(0, 1);
	}

	result = this.addFirstNameTags(result, withXML);
	return result;
    }

    private String getLastNames(boolean withXML) {
	String result = "";
	for (String lastName : this.lastNames) {
	    result += lastName + " ";
	}
	result = result.replaceFirst(" $", "");

	result = this.addLastNameTags(result, withXML);
	return result;
    }

    private String getLastNamesWithComma(boolean withXML) {
	String result = "";
	for (String lastName : this.lastNames) {
	    result += lastName + " ";
	}
	result = result.replaceFirst(" $", "");

	result += ",";

	result = this.addLastNameTags(result, withXML);
	return result;
    }

}
