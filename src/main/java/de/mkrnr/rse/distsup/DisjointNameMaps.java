package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DisjointNameMaps {
    private Map<String, Integer> firstNameMap;
    private Map<String, Integer> lastNameMap;

    private int sameNames = 0;
    private int sameNameCount = 0;
    private int totalCount = 0;

    public DisjointNameMaps(File firstNameFile, File lastNameFile) {
	this.firstNameMap = new HashMap<String, Integer>();
	this.lastNameMap = new HashMap<String, Integer>();

	this.addFirstNames(firstNameFile);
	this.addLastNames(lastNameFile);
	System.out.println("sameNames: " + (this.sameNames));
	System.out.println("sameNameCount: " + (this.sameNameCount));
	System.out.println("totalCount: " + this.totalCount);

    }

    public Map<String, Integer> getFirstNameMap() {
	return this.firstNameMap;
    }

    public Map<String, Integer> getLastNameMap() {
	return this.lastNameMap;
    }

    private void addFirstNames(File firstNameFile) {
	try {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(firstNameFile));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		String[] lineSplit = line.split("\t");
		if (lineSplit.length == 2) {
		    String firstName = lineSplit[0];

		    if (firstName.startsWith(".")) {
			continue;
		    }

		    if (Character.isLowerCase(firstName.charAt(0))) {
			continue;
		    }

		    Set<String> firstNameVariations = Name.getFirstNameVariations(firstName);
		    for (String firstNameVariation : firstNameVariations) {
			this.addName(firstNameVariation, Integer.parseInt(lineSplit[1]), this.firstNameMap,
				this.lastNameMap);
		    }
		} else {
		    bufferedReader.close();
		    throw new IllegalStateException("line in name file has not the format name tab count: " + line);
		}
	    }
	    bufferedReader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void addLastNames(File lastNameFile) {
	try {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(lastNameFile));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		String[] lineSplit = line.split("\t");
		if (lineSplit.length == 2) {

		    if (lineSplit[0].length() < 3) {
			continue;
		    }
		    this.addName(lineSplit[0], Integer.parseInt(lineSplit[1]), this.lastNameMap, this.firstNameMap);
		} else {
		    bufferedReader.close();
		    throw new IllegalStateException("line in name file has not the format name tab count: " + line);
		}
	    }
	    bufferedReader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void addName(String name, int nameCount, Map<String, Integer> mapToAdd, Map<String, Integer> otherMap) {
	this.totalCount += nameCount;
	if (otherMap.containsKey(name)) {
	    // TODO remove
	    this.sameNames++;
	    if (otherMap.get(name) > nameCount) {
		this.sameNameCount += nameCount;
	    } else {
		if (otherMap.get(name) < nameCount) {
		    this.sameNameCount += otherMap.get(name);
		}
	    }

	    if (otherMap.get(name) < nameCount) {
		otherMap.remove(name);
	    } else {
		return;
	    }
	}
	mapToAdd.put(name, nameCount);
    }
}
