package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;
import de.mkrnr.rse.util.FileHelper;

public class NameTagger {

    public static void main(String[] args) throws IOException {

	File firstNameFile = new File(args[0]);
	File lastNameFile = new File(args[1]);

	long startTime;
	long endTime;
	NameTagger nameTagger = new NameTagger(true);
	startTime = System.nanoTime();
	nameTagger.readNameMap(firstNameFile, GoddagNameStructure.NodeType.FIRST_NAME.toString());
	nameTagger.readNameMap(lastNameFile, GoddagNameStructure.NodeType.LAST_NAME.toString());
	endTime = System.nanoTime();
	System.out.println("Building tries took " + ((endTime - startTime) / 1000000) + " milliseconds");

	startTime = System.nanoTime();
	nameTagger.tagDirectory(new File(args[2]), new File(args[3]));
	// nameTagger.tagFile(new File(args[2]).listFiles()[1], new
	// File("/tmp/tagger-test.json"));
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

    private final String wordSplitRegex = "\\s";

    private GoddagNameStructure goddagNameStructure;

    private Map<String, Map<String, Integer>> nameMaps;
    private final String wordPropertyKey = "word";

    private Gson gson;

    public NameTagger(boolean prettyPrintJson) {
	this.nameMaps = new HashMap<String, Map<String, Integer>>();
	GsonBuilder gsonBuilder = new GsonBuilder();
	if (prettyPrintJson) {
	    gsonBuilder.setPrettyPrinting();
	}
	gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonSerializer());
	gsonBuilder.registerTypeAdapter(Node.class, Node.getJsonSerializer());
	this.gson = gsonBuilder.create();

    }

    public void createNameMap(Map<String, Integer> names, boolean createFirstNameVariations, String nodeType) {
	Map<String, Integer> nameMap = new HashMap<String, Integer>();
	for (Entry<String, Integer> nameEntry : names.entrySet()) {
	    if (createFirstNameVariations) {
		Set<String> firstNameVariations = Name.getFirstNameVariations(nameEntry.getKey());
		for (String firstNameVariation : firstNameVariations) {
		    nameMap.put(firstNameVariation, nameEntry.getValue());
		}
	    } else {
		nameMap.put(nameEntry.getKey(), nameEntry.getValue());
	    }
	}
	this.nameMaps.put(nodeType, nameMap);

    }

    public void readNameMap(File keywordFile, String nodeType) throws IOException {

	Map<String, Integer> nameMap = new HashMap<String, Integer>();
	BufferedReader bufferedReader = new BufferedReader(new FileReader(keywordFile));
	String line;
	while ((line = bufferedReader.readLine()) != null) {
	    String[] lineSplit = line.split("\\t");
	    nameMap.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
	}
	bufferedReader.close();

	this.createNameMap(nameMap, false, nodeType);
    }

    public void tagDirectory(File inputDirectory, File outputDirectory) throws IOException {
	if (!outputDirectory.exists()) {
	    outputDirectory.mkdirs();
	}
	try {
	    for (File inputFile : inputDirectory.listFiles()) {
		String[] inputFileNameSplit = inputFile.getName().split("\\.");
		String outputFileName = inputFileNameSplit[0] + ".json";
		this.tagFile(inputFile, new File(outputDirectory.getAbsolutePath() + File.separator + outputFileName));
	    }
	} catch (NullPointerException e) {
	    e.printStackTrace();
	}
    }

    public void tagFile(File inputFile, File outputFile) throws IOException {
	System.out.println("tag file: " + inputFile);
	String inputString = null;
	try {
	    inputString = FileHelper.readFile(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	Goddag goddag = this.tagString(inputString);

	// GoddagVisualizer goddagVisualizer = new GoddagVisualizer();
	// goddagVisualizer.visualize(this.goddagNameStructure.getGoddag());

	// System.out.println(this.goddagNameStructure);
	// JsonHelper.writeToFile(this.goddagNameStructure, outputFile);
	// System.exit(1);

	FileUtils.writeStringToFile(outputFile, this.gson.toJson(goddag, Goddag.class));
    }

    public Goddag tagString(String inputString) {
	this.initializeNameStructure(inputString);

	for (Entry<String, Map<String, Integer>> nameMap : this.nameMaps.entrySet()) {
	    this.tagLeafNodes(nameMap.getValue(), nameMap.getKey());
	}

	return this.goddagNameStructure.getGoddag();
    }

    private void initializeNameStructure(String inputString) {
	this.goddagNameStructure = new GoddagNameStructure();
	String[] inputStringSplit = inputString.split(this.wordSplitRegex);
	for (String inputStringPart : inputStringSplit) {
	    Node leafNode = this.goddagNameStructure.addAsLeafNode(this.goddagNameStructure.getGoddag().getRootNode(),
		    inputStringPart);
	    String inputStringPartWord = inputStringPart.replaceFirst("^(\\W)+", "").replaceFirst("(\\W+)$", "");
	    leafNode.addProperty(this.wordPropertyKey, inputStringPartWord);
	}
    }

    private GoddagNameStructure tagLeafNodes(Map<String, Integer> nameMap, String nodeType) {
	for (Node leafNode : this.goddagNameStructure.getLeafNodes()) {
	    if (nameMap.containsKey(leafNode.getProperty(this.wordPropertyKey))) {
		Node nonterminalNode = this.goddagNameStructure.createMatchNode(nodeType);
		this.goddagNameStructure.tagNodeAs(leafNode, nonterminalNode);
	    }
	}
	return this.goddagNameStructure;
    }
}
