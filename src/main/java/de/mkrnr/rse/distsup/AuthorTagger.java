package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;

import de.mkrnr.rse.util.FileHelper;

public class AuthorTagger {

    public static void main(String[] args) {

	AuthorTagger authorTagger = new AuthorTagger(new File(args[0]));
	authorTagger.tagFile(new File(args[1]), new File(args[2]));
    }

    private Set<Name> names;

    private Map<String, String> variationsMap;

    private TrieBuilder trieBuilder;

    public AuthorTagger(File nameFile) {

	System.out.println("read names");
	this.names = this.readNames(nameFile);
	System.out.println(this.names.size());
	System.out.println("generate variations");
	this.variationsMap = this.generateVariations(this.names);

	System.out.println("generate triebuilder");
	this.trieBuilder = this.createTrieBuilder(this.names);

    }

    public void tagFile(File inputFile, File outputFile) {
	System.out.println("tag file");
	String inputString = null;
	try {
	    inputString = FileHelper.readFile(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	Pattern closingAuthorTagsPattern = Pattern.compile("</\\p{L}*></\\p{L}*>$");

	Trie trie = this.trieBuilder.build();
	Collection<Token> tokens = trie.tokenize(inputString);
	StringBuffer taggedSequence = new StringBuffer();
	Iterator<Token> tokenIterator = tokens.iterator();
	while (tokenIterator.hasNext()) {
	    Token token = tokenIterator.next();
	    if (token.isMatch()) {
		String taggedToken = this.variationsMap.get(token.getFragment());

		// check if next token starts with a space
		if (tokenIterator.hasNext()) {
		    token = tokenIterator.next();
		    if (token.getFragment().startsWith(" ")) {
			// if it starts with a space, write it

			taggedSequence.append(taggedToken);
			taggedSequence.append(token.getFragment());
		    } else {
			// if it doesn't start with a space, modify the tag
			String currentToken = token.getFragment();

			String[] currentTokenSplit = currentToken.split(" ");
			if (currentTokenSplit.length > 0) {
			    Matcher matcher = closingAuthorTagsPattern.matcher(taggedToken);

			    String closingAuthorTagsMatch = "";
			    if (matcher.find()) {
				closingAuthorTagsMatch = matcher.group(0);
			    } else {
				throw new IllegalStateException(
					"Author tags do not have two closing tags at the end: " + taggedToken);
			    }
			    taggedSequence.append(taggedToken.replaceFirst(closingAuthorTagsMatch, "")
				    + currentTokenSplit[0] + closingAuthorTagsMatch);
			} else {
			    taggedSequence.append(taggedToken);

			}

			if (currentTokenSplit.length > 1) {
			    taggedSequence.append(" ");
			    for (int i = 1; i < currentTokenSplit.length; i++) {
				taggedSequence.append(currentTokenSplit[i] + " ");
			    }
			}
		    }
		}
		// taggedSequence.append(taggedToken);
	    } else {
		// token is not a match
		taggedSequence.append(token.getFragment());
	    }

	}
	// for (Token token : tokens) {
	// if (token.isMatch()) {
	// // taggedSequence.append("<author>");
	// taggedSequence.append(this.variationsMap.get(token.getFragment()));
	// // taggedSequence.append("</author>");

	// } else {
	// taggedSequence.append(token.getFragment());
	// }
	// }
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
	// int i = 0;
	for (Name name : names) {
	    // System.out.println(i++);
	    String[] variations = name.getVariations(false);
	    for (String variation : variations) {
		trieBuilder.addKeyword(variation);
	    }
	}
	return trieBuilder;
    }

    private Map<String, String> generateVariations(Set<Name> names) {
	Map<String, String> variationsMap = new HashMap<String, String>();
	int i = 0;
	for (Name name : names) {
	    System.out.println(i++);
	    Map<String, String> nameVariationsMap = name.getVariationsXMLMap();
	    for (Entry<String, String> entry : nameVariationsMap.entrySet()) {
		variationsMap.put(entry.getKey(), entry.getValue());
	    }
	}
	return variationsMap;

    }

    private Set<Name> readNames(File nameFile) {
	Set<Name> names = new HashSet<Name>();
	try {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(nameFile));
	    String line;
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

			String[] lastNames = lineSplit[1].split("\\s");
			names.add(new Name(firstNames, lastNames));
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
