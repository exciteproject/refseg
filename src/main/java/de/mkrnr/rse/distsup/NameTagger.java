package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;

import de.mkrnr.goddag.LeafNode;
import de.mkrnr.rse.distsup.GoddagNameStructure.NodeType;
import de.mkrnr.rse.util.FileHelper;

public class NameTagger {

    public static void main(String[] args) throws IOException {

	File firstNameFile = new File(args[0]);
	File lastNameFile = new File(args[1]);

	long startTime;
	long endTime;
	NameTagger nameTagger = new NameTagger();
	startTime = System.nanoTime();
	nameTagger.createTrie(firstNameFile, GoddagNameStructure.NodeType.FIRST_NAME);
	nameTagger.createTrie(lastNameFile, GoddagNameStructure.NodeType.LAST_NAME);
	endTime = System.nanoTime();
	System.out.println("Building tries took " + ((endTime - startTime) / 1000000) + " milliseconds");

	startTime = System.nanoTime();
	nameTagger.tagDirectory(new File(args[2]), new File(args[3]));
	// nameTagger.tagFile(new File(args[2]), new File(args[3]));
	endTime = System.nanoTime();
	System.out.println("Tagging took " + ((endTime - startTime) / 1000000) + " milliseconds");

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

    // map containing the tag name as key and the according Trie as value
    private Map<NodeType, Trie> tries;

    private final String wordSplitRegex = "\\s";
    private GoddagNameStructure goddagNameStructure;

    private LeafNode currentLeafNode;

    private Iterator<LeafNode> leafNodeIterator;

    public NameTagger() {
	this.tries = new HashMap<GoddagNameStructure.NodeType, Trie>();
    }

    public void createTrie(File keywordFile, NodeType nodeType) throws IOException {

	// TODO store as map with counts
	List<String> keywords = new ArrayList<String>();
	BufferedReader bufferedReader = new BufferedReader(new FileReader(keywordFile));
	String line;
	while ((line = bufferedReader.readLine()) != null) {
	    String[] lineSplit = line.split("\\t");
	    keywords.add(lineSplit[0]);
	}
	bufferedReader.close();

	this.createTrie(keywords, nodeType);
    }

    public void createTrie(List<String> keywords, NodeType nodeType) {
	System.out.println("read names");

	System.out.println("generate triebuilder");

	TrieBuilder trieBuilder = Trie.builder().onlyWholeWords();
	for (String keyword : keywords) {
	    trieBuilder.addKeyword(keyword);
	}

	this.tries.put(nodeType, trieBuilder.build());
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
	String inputString = null;
	try {
	    inputString = FileHelper.readFile(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	this.initializeNameStructure(inputString);
	for (Entry<NodeType, Trie> trieEntry : this.tries.entrySet()) {
	    this.tagString(inputString, trieEntry.getValue(), trieEntry.getKey());
	}

	// System.out.println(this.goddagNameStructure);
	try {
	    // TODO serialize goddagNameStructure instead
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	    bufferedWriter.write(this.goddagNameStructure.toString());
	    bufferedWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public GoddagNameStructure tagString(String inputString, Trie trie, NodeType nodeType) {

	this.leafNodeIterator = this.goddagNameStructure.getLeafNodeIterator();
	if (this.leafNodeIterator.hasNext()) {
	    this.currentLeafNode = this.leafNodeIterator.next();
	} else {
	    throw new IllegalStateException("leafNodeIterator was empty after initialization");
	}

	Collection<Token> tokens = trie.tokenize(inputString);
	Iterator<Token> tokenIterator = tokens.iterator();

	while (tokenIterator.hasNext()) {
	    final Token token = tokenIterator.next();
	    // System.out.println(token.getFragment());
	    if (token.isMatch()) {
		this.tagMatch(token.getFragment(), nodeType);
	    } else {
		this.moveCurrentLeafNode(token.getFragment());
	    }
	}
	// System.out.println(this.goddagNameStructure.toString());
	return this.goddagNameStructure;
    }

    private void initializeNameStructure(String inputString) {
	this.goddagNameStructure = new GoddagNameStructure();
	String[] inputStringSplit = inputString.split(this.wordSplitRegex);
	for (String inputStringPart : inputStringSplit) {
	    this.goddagNameStructure.addAsLeafNode(inputStringPart);
	}
    }

    private void moveCurrentLeafNode(String text) {
	String[] textSplit = text.split(this.wordSplitRegex);

	for (String textPart : textSplit) {
	    while (!this.currentLeafNode.getLabel().contains(textPart)) {
		this.currentLeafNode = this.currentLeafNode.getNextLeafNode();
		if (this.currentLeafNode == null) {
		    throw new IllegalStateException("reached end of leafNodes while searching for match: " + textPart);
		}
	    }
	}
    }

    private void tagMatch(String match, NodeType nodeType) {

	String[] matchSplit = match.split(this.wordSplitRegex);

	// move current leafNode until the last word of matchSplit
	while (!this.currentLeafNode.getLabel().contains(matchSplit[matchSplit.length - 1])) {
	    this.currentLeafNode = this.currentLeafNode.getNextLeafNode();
	    if (this.currentLeafNode == null) {
		throw new IllegalStateException("reached end of leafNodes while searching for match");
	    }
	}

	ArrayList<LeafNode> matchingLeafNodes = new ArrayList<LeafNode>();
	LeafNode currentMatchingLeafNode = this.currentLeafNode;

	for (@SuppressWarnings("unused")
	String element : matchSplit) {
	    matchingLeafNodes.add(0, currentMatchingLeafNode);
	    currentMatchingLeafNode = currentMatchingLeafNode.getPreviousLeafNode();
	}
	this.goddagNameStructure.tagNodesAs(matchingLeafNodes.toArray(new LeafNode[0]), nodeType);
    }
}
