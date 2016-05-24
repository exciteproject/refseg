package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import de.mkrnr.goddag.Node;
import de.mkrnr.rse.util.FileHelper;

public class NameTagger {

    public static void main(String[] args) throws IOException {

	File firstNameFile = new File(args[0]);
	File lastNameFile = new File(args[1]);

	long startTime;
	long endTime;
	NameTagger nameTagger = new NameTagger();
	startTime = System.nanoTime();
	nameTagger.createTrie(firstNameFile, GoddagNameStructure.NodeType.FIRST_NAME.toString());
	nameTagger.createTrie(lastNameFile, GoddagNameStructure.NodeType.LAST_NAME.toString());
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

    private Map<String, Trie> tries;

    private final String wordSplitRegex = "\\s";
    private GoddagNameStructure goddagNameStructure;

    private List<Node> leafNodes;

    private int currentLeafNodeIndex;

    public NameTagger() {
	this.tries = new HashMap<String, Trie>();
    }

    // TODO set Map<String,Integer> as input and generate first name variations
    public void createTrie(File keywordFile, String nodeType) throws IOException {

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

    public void createTrie(List<String> keywords, String nodeType) {
	System.out.println("read names");

	System.out.println("generate triebuilder");

	TrieBuilder trieBuilder = Trie.builder().onlyWholeWords();
	for (String keyword : keywords) {
	    trieBuilder.addKeyword(keyword);
	}

	this.tries.put(nodeType, trieBuilder.build());
    }

    public void tagDirectory(File inputDirectory, File outputDirectory) throws IOException {
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
	for (Entry<String, Trie> trieEntry : this.tries.entrySet()) {
	    this.tagString(inputString, trieEntry.getValue(), trieEntry.getKey());
	}

	System.out.println(this.goddagNameStructure);
	// JsonHelper.writeToFile(this.goddagNameStructure, outputFile);
	// System.exit(1);
    }

    public GoddagNameStructure tagString(String inputString, Trie trie, String nodeType) {

	this.currentLeafNodeIndex = 0;

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
	this.leafNodes = this.goddagNameStructure.getLeafNodes();
    }

    private void moveCurrentLeafNode(String text) {
	String[] textSplit = text.split(this.wordSplitRegex);

	for (String textPart : textSplit) {
	    while (!this.leafNodes.get(this.currentLeafNodeIndex).getLabel().contains(textPart)) {
		this.currentLeafNodeIndex++;
		if (this.currentLeafNodeIndex >= this.leafNodes.size()) {
		    throw new IllegalStateException("reached end of leafNodes while searching for match: " + textPart);
		}
	    }
	}
    }

    private void tagMatch(String match, String nodeType) {

	String[] matchSplit = match.split(this.wordSplitRegex);

	// move current leafNode until the last word of matchSplit
	while (!this.leafNodes.get(this.currentLeafNodeIndex).getLabel().contains(matchSplit[matchSplit.length - 1])) {
	    this.currentLeafNodeIndex++;
	    if (this.currentLeafNodeIndex >= this.leafNodes.size()) {
		throw new IllegalStateException("reached over the end of leafNodes while searching for match");
	    }
	}

	ArrayList<Node> matchingLeafNodes = new ArrayList<Node>();
	int currentMatchingLeafNodeIndex = this.currentLeafNodeIndex;

	for (@SuppressWarnings("unused")
	String element : matchSplit) {
	    matchingLeafNodes.add(0, this.leafNodes.get(currentMatchingLeafNodeIndex));
	    currentMatchingLeafNodeIndex--;
	    // if(currentMatchingLeafNodeIndex<0){
	    // throw new IllegalStateException("reached end of leafNodes while
	    // searching for match: " + textPart);
	    // }
	}
	this.goddagNameStructure.tagNodesAs(matchingLeafNodes.toArray(new Node[0]), nodeType);
    }
}
