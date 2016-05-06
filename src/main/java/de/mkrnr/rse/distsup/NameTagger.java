package de.mkrnr.rse.distsup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;

import de.mkrnr.rse.util.FileHelper;

public class NameTagger {

    public static void main(String[] args) {

	File firstNameFile = new File(args[0]);
	File lastNameFile = new File(args[1]);
	NameTagger authorTagger = new NameTagger("firstName", "lastName");
	authorTagger.createTries(firstNameFile, lastNameFile);
	long startTime = System.currentTimeMillis();
	authorTagger.tagDirectory(new File(args[2]), new File(args[3]));
	long endTime = System.currentTimeMillis();
	System.out.println("This took " + (endTime - startTime) + " milliseconds");

	// Getting the runtime reference from system
	Runtime runtime = Runtime.getRuntime();

	int mb = 1024 * 1024;
	System.out.println("##### Heap utilization statistics [MB] #####");

	// Print used memory
	System.out.println("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / mb));

	// Print free memory
	System.out.println("Free Memory:" + (runtime.freeMemory() / mb));

	// Print total available memory
	System.out.println("Total Memory:" + (runtime.totalMemory() / mb));

	// Print Maximum available memory
	System.out.println("Max Memory:" + (runtime.maxMemory() / mb));
    }

    private final String firstNameLabel;

    private final String lastNameLabel;

    // map containing the tag name as key and the according Trie as value
    private Map<String, Trie> tries;

    public NameTagger(String firstNameLabel, String lastNameLabel) {
	this.firstNameLabel = firstNameLabel;
	this.lastNameLabel = lastNameLabel;

    }

    public void createTries(File firstNameFile, File lastNameFile) {
	System.out.println("read names");
	DisjointNameMaps nameMaps = new DisjointNameMaps(firstNameFile, lastNameFile);

	System.out.println("generate triebuilder");

	this.tries = new HashMap<String, Trie>();
	this.tries.put(this.firstNameLabel, this.createTrieBuilder(nameMaps.getFirstNameMap()).build());
	this.tries.put(this.lastNameLabel, this.createTrieBuilder(nameMaps.getLastNameMap()).build());
    }

    public void tagDirectory(File inputDirectory, File outputDirectory) {
	if (!outputDirectory.exists()) {
	    outputDirectory.mkdirs();
	}
	try {
	    for (File inputFile : inputDirectory.listFiles()) {
		this.tagFile(inputFile,
			new File(outputDirectory.getAbsolutePath() + File.separator + inputFile.getName()));
	    }
	} catch (NullPointerException e) {
	    e.printStackTrace();
	}
    }

    public void tagFile(File inputFile, File outputFile) {
	System.out.println("tag file");
	String inputString = null;
	try {
	    inputString = FileHelper.readFile(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	String taggedFirstNameString = this.tagString(inputString, this.tries.get(this.firstNameLabel),
		this.firstNameLabel);
	String taggedFirstAndLastNameString = this.tagString(taggedFirstNameString, this.tries.get(this.lastNameLabel),
		this.lastNameLabel);

	try {
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	    bufferedWriter.write(taggedFirstAndLastNameString);
	    bufferedWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private TrieBuilder createTrieBuilder(Map<String, Integer> nameMap) {
	TrieBuilder trieBuilder = Trie.builder().removeOverlaps().onlyWholeWords();
	for (Entry<String, Integer> nameMapEntry : nameMap.entrySet()) {
	    trieBuilder.addKeyword(nameMapEntry.getKey());
	}
	return trieBuilder;
    }

    private String tagString(String inputString, Trie trie, String tagName) {

	Collection<Token> tokens = trie.tokenize(inputString);
	StringBuffer taggedSequence = new StringBuffer();
	Iterator<Token> tokenIterator = tokens.iterator();
	while (tokenIterator.hasNext()) {
	    Token token = tokenIterator.next();
	    if (token.isMatch()) {
		taggedSequence.append("<" + tagName + ">" + token.getFragment() + "</" + tagName + ">");
	    } else {
		taggedSequence.append(token.getFragment());
	    }

	}
	return taggedSequence.toString();

    }

}
