package de.mkrnr.rse.distsup;

import java.util.HashSet;
import java.util.Set;

public class Name {
    public static void main(String[] args) {
	String[] firstName = { "Markus", "Anton" };
	String[] lastName = { "Müller" };
	Name name = new Name(firstName, lastName);
	String[] firstNameVariations = name.getFirstNameVariations();
	for (String firstNameVariation : firstNameVariations) {
	    System.out.println(firstNameVariation);
	}
	System.out.println(firstNameVariations.length);

	System.out.println(name.getLastName());
    }

    private String[] firstNames;
    private String[] lastNames;

    public Name(String[] firstNames, String[] lastNames) {
	this.firstNames = firstNames;
	this.lastNames = lastNames;
    }

    public String getFirstName() {
	String result = "";
	for (String firstNames : this.firstNames) {
	    result += firstNames + " ";
	}
	result = result.replaceFirst(" $", "");

	return result;
    }

    public String[] getFirstNameVariations() {
	Set<String> firstNameVariations = new HashSet<String>();
	firstNameVariations.add(this.getFirstName());
	firstNameVariations.add(this.getFirstNDot());
	firstNameVariations.add(this.getFirstN());
	firstNameVariations.add(this.getFDotSpaceNDot());
	firstNameVariations.add(this.getFDotNDot());
	firstNameVariations.add(this.getFN());
	return firstNameVariations.toArray(new String[0]);
    }

    public String getLastName() {
	String result = "";
	for (String lastName : this.lastNames) {
	    result += lastName + " ";
	}
	result = result.replaceFirst(" $", "");

	return result;
    }

    private String getFDotNDot() {
	String result = "";
	for (String firstName : this.firstNames) {
	    result += firstName.substring(0, 1) + ".";
	}

	return result;
    }

    private String getFDotSpaceNDot() {
	String result = "";
	for (String firstName : this.firstNames) {
	    result += firstName.substring(0, 1) + ". ";
	}
	result = result.replaceFirst(" $", "");

	return result;
    }

    private String getFirstN() {
	String result = "";
	result += this.firstNames[0];
	if (this.firstNames.length > 1) {
	    for (int i = 1; i < this.firstNames.length; i++) {
		result += " " + this.firstNames[i].substring(0, 1);
	    }
	}
	return result;
    }

    private String getFirstNDot() {
	String result = "";
	result += this.firstNames[0];
	if (this.firstNames.length > 1) {
	    for (int i = 1; i < this.firstNames.length; i++) {
		result += " " + this.firstNames[i].substring(0, 1) + ".";
	    }
	}

	return result;
    }

    private String getFN() {
	String result = "";
	for (String firstName : this.firstNames) {
	    result += firstName.substring(0, 1);
	}

	return result;
    }

}
