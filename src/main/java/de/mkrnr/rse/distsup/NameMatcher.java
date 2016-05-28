package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;
import de.mkrnr.rse.distsup.GoddagNameStructure.NodeType;

public class NameMatcher {

    public static void main(String[] args) throws IOException {
	File nameFile = new File(args[0]);
	File taggedDir = new File(args[1]);
	File outputDir = new File(args[2]);

	long startTime;
	long endTime;
	startTime = System.nanoTime();
	NameMatcher nameMatcher = new NameMatcher(nameFile, true);
	endTime = System.nanoTime();
	System.out.println("Building took " + (((endTime - startTime)) / 1000000) + " milliseconds");

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

	startTime = System.nanoTime();
	nameMatcher.matchDirectory(taggedDir, outputDir);

	// nameMatcher.matchFile(taggedDir.listFiles()[1], new
	// File("/tmp/tagger-test.json"));
	endTime = System.nanoTime();
	System.out.println("Matching took " + (((endTime - startTime)) / 1000000) + " milliseconds");
    }

    // generate name lookup
    // read file
    // remove linebreaks
    // sliding window:
    // at position i, check if name at i with name at i+1 is in database (check
    // all possibilities)
    // if yes, get context and print it/store in array

    /**
     * Format of names: Map<lastName,Map<firstNameVariation,count>>
     */
    private Map<String, Map<String, Integer>> namesLookup;
    private Gson gson;
    private final String wordPropertyKey = "word";
    private GoddagNameStructure goddagNameStructure;

    /**
     * Constructor that generates a lookup for the names in nameFile TODO:
     * specify format of nameFile
     *
     * @param nameFile
     * @throws IOException
     */
    public NameMatcher(File nameFile, boolean prettyPrintJson) throws IOException {
	this.namesLookup = this.generateNamesLookUp(nameFile);
	GsonBuilder gsonBuilder = new GsonBuilder();
	if (prettyPrintJson) {
	    gsonBuilder.setPrettyPrinting();
	}
	gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
	gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonSerializer());
	gsonBuilder.registerTypeAdapter(Node.class, Node.getJsonSerializer());
	this.gson = gsonBuilder.create();

    }

    public void matchDirectory(File inputDirectory, File outputDirectory)
	    throws JsonSyntaxException, JsonIOException, IOException {
	if (!outputDirectory.exists()) {
	    outputDirectory.mkdirs();
	}
	for (File inputFile : inputDirectory.listFiles()) {
	    this.matchFile(inputFile,
		    new File((outputDirectory.getAbsolutePath() + File.separator + inputFile.getName())));
	}
    }

    public void matchFile(File inputFile, File outputFile) throws JsonSyntaxException, JsonIOException, IOException {
	Goddag goddag = this.gson.fromJson(new FileReader(inputFile), Goddag.class);
	this.goddagNameStructure = new GoddagNameStructure(goddag);

	List<Node> leafNodes = this.goddagNameStructure.getLeafNodes();
	for (int leafNodeIndex = 0; leafNodeIndex < leafNodes.size(); leafNodeIndex++) {
	    Node lastNameParentNode = this.getParentNode(leafNodes.get(leafNodeIndex), NodeType.LAST_NAME);
	    if (lastNameParentNode != null) {
		this.searchAuthorsBefore(leafNodes, leafNodeIndex, lastNameParentNode);
		this.searchAuthorsAfter(leafNodes, leafNodeIndex, lastNameParentNode);
	    }
	}
	// System.out.println(this.goddagNameStructure.toString());

	// GoddagVisualizer goddagVisualizer = new GoddagVisualizer();
	// goddagVisualizer.visualize(goddag);

	FileUtils.writeStringToFile(outputFile, this.gson.toJson(this.goddagNameStructure.getGoddag(), Goddag.class));

    }

    private Map<String, Map<String, Integer>> generateNamesLookUp(File nameFile) throws IOException {
	Map<String, Map<String, Integer>> namesLookup = new HashMap<String, Map<String, Integer>>();
	BufferedReader nameReader = new BufferedReader(new FileReader(nameFile));

	String line;
	while ((line = nameReader.readLine()) != null) {
	    String[] lineSplit = line.split("\t");
	    if (lineSplit.length != 3) {
		nameReader.close();
		throw new IOException("line length != 3: \"" + line + "\"");
	    }
	    Set<String> firstNameVariations = Name.getFirstNameVariations(lineSplit[0]);
	    String lastName = lineSplit[1];
	    Map<String, Integer> lastNameMap;
	    int count = Integer.parseInt(lineSplit[2]);
	    if (namesLookup.containsKey(lastName)) {
		lastNameMap = namesLookup.get(lastName);
	    } else {
		lastNameMap = new HashMap<String, Integer>();
		namesLookup.put(lastName, lastNameMap);
	    }
	    for (String firstNameVariation : firstNameVariations) {
		if (lastNameMap.containsKey(firstNameVariation)) {
		    lastNameMap.put(firstNameVariation, lastNameMap.get(firstNameVariation) + count);
		} else {
		    lastNameMap.put(firstNameVariation, count);
		}
	    }
	}
	nameReader.close();

	return namesLookup;
    }

    private Node getParentNode(Node node, NodeType parentNodeType) {
	for (Node firstNameBeforeParent : node.getParents()) {
	    if (firstNameBeforeParent.getLabel().equals(parentNodeType.toString())) {
		return firstNameBeforeParent;
	    }
	}
	return null;
    }

    private boolean isName(List<Node> firstNameParentNodes, Node lastNameNode) {
	String generatedFirstName = "";
	for (Node firstNameNode : firstNameParentNodes) {
	    generatedFirstName += firstNameNode.getFirstChild().getProperty(this.wordPropertyKey) + " ";
	}
	generatedFirstName = generatedFirstName.replaceFirst(" $", "");

	String lastName = lastNameNode.getProperty(this.wordPropertyKey);
	if (this.namesLookup.containsKey(lastName)) {
	    if (this.namesLookup.get(lastName).containsKey(generatedFirstName)) {
		return true;
	    }
	}
	return false;

    }

    private void searchAuthorsAfter(List<Node> leafNodes, int lastNameIndex, Node lastNameParentNode) {
	List<Node> firstNamesAfter = new ArrayList<Node>();
	for (int indexAfterLeafNodeIndex = lastNameIndex + 1; indexAfterLeafNodeIndex < leafNodes
		.size(); indexAfterLeafNodeIndex++) {
	    Node firstNameParent = this.getParentNode(leafNodes.get(indexAfterLeafNodeIndex), NodeType.FIRST_NAME);
	    if (firstNameParent == null) {
		break;
	    } else {
		firstNamesAfter.add(firstNameParent);
	    }
	}

	for (int firstNameIndex = 0; firstNameIndex < firstNamesAfter.size(); firstNameIndex++) {
	    List<Node> currentFirstNameBefore = new ArrayList<Node>();
	    for (int firstNameGenerationIndex = firstNameIndex; firstNameGenerationIndex < firstNamesAfter
		    .size(); firstNameGenerationIndex++) {
		currentFirstNameBefore.add(firstNamesAfter.get(firstNameGenerationIndex));
	    }

	    if (this.isName(currentFirstNameBefore, leafNodes.get(lastNameIndex))) {
		List<Node> nodesToTag = new ArrayList<Node>();
		nodesToTag.add(lastNameParentNode);
		for (Node currentFirstNameNode : currentFirstNameBefore) {
		    nodesToTag.add(currentFirstNameNode);
		}

		this.goddagNameStructure.tagNodesAs(nodesToTag.toArray(new Node[0]), NodeType.AUTHOR.toString());
	    }
	}

    }

    private void searchAuthorsBefore(List<Node> leafNodes, int leafNodeIndex, Node lastNameParentNode) {
	List<Node> firstNamesBefore = new ArrayList<Node>();
	for (int indexBeforeLeafNodeIndex = leafNodeIndex
		- 1; indexBeforeLeafNodeIndex >= 0; indexBeforeLeafNodeIndex--) {
	    Node firstNameParent = this.getParentNode(leafNodes.get(indexBeforeLeafNodeIndex), NodeType.FIRST_NAME);
	    if (firstNameParent == null) {
		break;
	    } else {
		firstNamesBefore.add(0, firstNameParent);
	    }
	}

	for (int firstNameIndex = 0; firstNameIndex < firstNamesBefore.size(); firstNameIndex++) {
	    List<Node> currentFirstNameBefore = new ArrayList<Node>();
	    for (int firstNameGenerationIndex = firstNameIndex; firstNameGenerationIndex < firstNamesBefore
		    .size(); firstNameGenerationIndex++) {
		currentFirstNameBefore.add(firstNamesBefore.get(firstNameGenerationIndex));
	    }

	    if (this.isName(currentFirstNameBefore, leafNodes.get(leafNodeIndex))) {
		List<Node> nodesToTag = new ArrayList<Node>();
		for (Node currentFirstNameNode : currentFirstNameBefore) {
		    nodesToTag.add(currentFirstNameNode);
		}
		nodesToTag.add(lastNameParentNode);

		this.goddagNameStructure.tagNodesAs(nodesToTag.toArray(new Node[0]), NodeType.AUTHOR.toString());
	    }
	}

    }
}
