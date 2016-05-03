package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;

import de.mkrnr.rse.util.FileHelper;

public class NameTagger {

    public static void main(String[] args) {

	NameTagger authorTagger = new NameTagger();
	// Getting the runtime reference from system
	// authorTagger.tagFile(new File(args[1]), new File(args[2]));
	long startTime = System.currentTimeMillis();
	authorTagger.createTrie(new File(args[0]));
	long endTime = System.currentTimeMillis();
	System.out.println("Creating the TrieBuilder took " + (endTime - startTime) + " milliseconds");

	authorTagger.tagDirectory(new File(args[1]), new File(args[2]));

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

    private Set<Name> names;

    private TrieBuilder trieBuilder;

    private Trie trie;

    public void createTrie(File nameFile) {
	System.out.println("read names");
	this.names = this.readNames(nameFile);
	System.out.println(this.names.size());

	System.out.println("generate triebuilder");
	this.trieBuilder = this.createTrieBuilder(this.names);
	this.trie = this.trieBuilder.build();
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

	Collection<Token> tokens = this.trie.tokenize(inputString);
	StringBuffer taggedSequence = new StringBuffer();
	Iterator<Token> tokenIterator = tokens.iterator();
	while (tokenIterator.hasNext()) {
	    Token token = tokenIterator.next();
	    if (token.isMatch()) {
		taggedSequence.append("<name>" + token.getFragment() + "</name>");
	    } else {
		// token is not a match
		taggedSequence.append(token.getFragment());
	    }

	}
	try {
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	    bufferedWriter.write(taggedSequence.toString());
	    bufferedWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private TrieBuilder createTrieBuilder(Set<Name> names) {
	TrieBuilder trieBuilder = Trie.builder().removeOverlaps().onlyWholeWords();
	// TrieBuilder trieBuilder = Trie.builder().onlyWholeWords();
	int i = 0;
	for (Name name : names) {
	    // System.out.println(i++);
	    String[] firstNameVariations = name.getFirstNameVariations();
	    for (String firstNameVariation : firstNameVariations) {
		i++;
		trieBuilder.addKeyword(firstNameVariation);
	    }
	    trieBuilder.addKeyword(name.getLastName());
	}
	System.out.println("total number of author variations in triebuilder: " + i);
	return trieBuilder;
    }

    private Set<Name> readNames(File nameFile) {
	Set<Name> names = new HashSet<Name>();
	try {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(nameFile));
	    String line;
	    int test = 0;
	    while ((line = bufferedReader.readLine()) != null) {
		String[] lineSplit = line.split("\t");
		if (lineSplit.length == 3) {
		    if (Integer.parseInt(lineSplit[2]) > 0) {
			String[] firstNames = lineSplit[0].split("\\s");

			// skip lower case first names
			if (Character.isLowerCase(firstNames[0].charAt(0))) {
			    continue;
			}

			if (firstNames[0].charAt(0) == '.') {
			    continue;
			}

			if (lineSplit[1].length() < 3) {
			    continue;
			}

			String[] lastNames = lineSplit[1].split("\\s");

			names.add(new Name(firstNames, lastNames));
			test++;
			if (test >= 750000) {
			    break;
			}
		    }
		}

	    }
	    bufferedReader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return names;
    }

}
