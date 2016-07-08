package de.mkrnr.rse.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;
import de.mkrnr.rse.distsup.GoddagNameStructure;
import de.mkrnr.rse.util.JsonHelper;

//TODO refactor the whole class...
public class NameConstraintBuilder {

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
	NameConstraintBuilder nameConstraintBuilder = new NameConstraintBuilder();

	String jsonFileToUsePath = args[0];
	String goddagDictionaryPath = args[1];
	String constraintsOutputFilePath = args[2];
	double nonAuthorRatio = Double.parseDouble(args[3]);
	double bFnPercentage = Double.parseDouble(args[4]);
	double bLnPercentage = Double.parseDouble(args[5]);
	double iFnPercentage = Double.parseDouble(args[6]);
	double iLnPercentage = Double.parseDouble(args[7]);
	double iOPercentage = Double.parseDouble(args[8]);
	double oPercentage = Double.parseDouble(args[9]);
	boolean fixPercentages = Boolean.parseBoolean(args[10]);

	File goddagDirectory = new File(goddagDictionaryPath);

	@SuppressWarnings("unchecked")
	List<File> filesToUse = (List<File>) JsonHelper.readFromFile(new TypeToken<List<File>>() {
	}.getType(), new File(jsonFileToUsePath));
	Set<String> fileIdsToUse = new HashSet<String>();
	for (File inputFile : filesToUse) {
	    fileIdsToUse.add(FilenameUtils.removeExtension(inputFile.getName()));
	}

	nameConstraintBuilder.extractAuthorStatistics(goddagDirectory, fileIdsToUse, nonAuthorRatio, bFnPercentage,
		bLnPercentage, iFnPercentage, iLnPercentage, iOPercentage, oPercentage, fixPercentages);
	// nameConstraintBuilder.printContraints();
	nameConstraintBuilder.writeDistributions(new File(constraintsOutputFilePath), "B-FN", "B-LN", "I-FN", "I-LN",
		"I-O", "O");
    }

    private Gson gson;

    private Map<String, NameDistribution> nameDistributions;

    public NameConstraintBuilder() {
	GsonBuilder gsonBuilder = new GsonBuilder();
	gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
	this.gson = gsonBuilder.create();

	this.nameDistributions = new HashMap<String, NameDistribution>();

    }

    /**
     *
     * @param fileIdsToUse
     * @param goddagInputFile
     * @param nonAuthorRatio
     *            if 2.0: amount of non-author tags two times the amount of
     *            author tags
     * @param bFnPercentage
     * @param bLnPercentage
     * @param iFnPercentage
     * @param iLnPercentage
     * @param iOPercentage
     * @param oPercentage
     * @param fixPercentages
     * @throws JsonSyntaxException
     * @throws JsonIOException
     * @throws FileNotFoundException
     */
    public void extractAuthorStatistics(File goddagDirectory, Set<String> fileIdsToUse, double nonAuthorRatio,
	    double bFnPercentage, double bLnPercentage, double iFnPercentage, double iLnPercentage, double iOPercentage,
	    double oPercentage, boolean fixPercentages)
	    throws JsonSyntaxException, JsonIOException, FileNotFoundException {
	double totalPercentage = bFnPercentage + bLnPercentage + iFnPercentage + iLnPercentage + iOPercentage
		+ oPercentage;
	if ((totalPercentage - 1.0) != 0) {
	    throw new IllegalArgumentException("percentages don't sum up to one");
	}

	int totalBFnCount = 0;
	int totalBLnCount = 0;
	int totalIFnCount = 0;
	int totalILnCount = 0;
	int totalLeafCount = 0;

	// go over goddag files the first time to get author tags
	for (File goddagFile : goddagDirectory.listFiles()) {
	    String goddagFileId = FilenameUtils.removeExtension(goddagFile.getName());
	    if (fileIdsToUse.contains(goddagFileId)) {
		Goddag goddag = this.gson.fromJson(new FileReader(goddagFile), Goddag.class);
		GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);
		totalLeafCount += goddagNameStructure.getLeafNodes().size();

		// iterate over goddag tree to count first names and last names
		for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
		    if (rootChildNode.getLabel().equals(GoddagNameStructure.NodeType.AUTHOR.toString())) {
			boolean isBeginning = true;
			for (Node authorChildNode : rootChildNode.getChildren()) {
			    String nameWord = authorChildNode.getFirstChild().getLabel();
			    if (!this.nameDistributions.containsKey(nameWord)) {
				this.nameDistributions.put(nameWord, new NameDistribution());
			    }
			    if ((authorChildNode.getLabel()
				    .equals(GoddagNameStructure.NodeType.FIRST_NAME.toString()))) {
				if (isBeginning) {
				    this.nameDistributions.get(nameWord).bFirstNameCount += 1;
				    totalBFnCount++;
				} else {
				    this.nameDistributions.get(nameWord).iFirstNameCount += 1;
				    totalIFnCount++;
				}
			    } else {
				if (authorChildNode.getLabel()
					.equals(GoddagNameStructure.NodeType.LAST_NAME.toString())) {
				    if (isBeginning) {
					this.nameDistributions.get(nameWord).bLastNameCount += 1;
					totalBLnCount++;
				    } else {
					this.nameDistributions.get(nameWord).iLastNameCount += 1;
					totalILnCount++;
				    }
				}
			    }
			    if (isBeginning) {
				isBeginning = false;
			    }
			}
		    }
		}
	    }
	}

	System.out.println("totalLeafCount: " + totalLeafCount);
	int totalTaggedCount = totalBFnCount + totalBLnCount + totalIFnCount + totalILnCount;
	System.out.println("totalTaggedCount: " + totalTaggedCount);
	int totalUntaggedCount = totalLeafCount - totalTaggedCount;
	System.out.println("totalUntaggedCount: " + totalUntaggedCount);

	if (fixPercentages) {
	    System.out.println(totalBFnCount);
	    System.out.println(totalBLnCount);
	    System.out.println(totalIFnCount);
	    System.out.println(totalILnCount);

	    System.out.println("---");
	}

	// TODO check counts
	double nonAuthorPercentage = (2 * totalTaggedCount) / totalLeafCount;
	// go over goddag files a second time to get non-author tags based on
	// statistics
	for (File goddagFile : goddagDirectory.listFiles()) {
	    String goddagFileId = FilenameUtils.removeExtension(goddagFile.getName());
	    if (fileIdsToUse.contains(goddagFileId)) {
		Goddag goddag = this.gson.fromJson(new FileReader(goddagFile), Goddag.class);
		GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);

		for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
		    if (rootChildNode.getLabel().equals(GoddagNameStructure.NodeType.AUTHOR.toString())) {

			if (Math.random() < nonAuthorPercentage) {
			    Node childNode = rootChildNode;
			    while (childNode.hasChildren()) {
				childNode = childNode.getFirstChild();
			    }
			    String word = childNode.getLabel();
			    if (word.isEmpty()) {
				continue;
			    }
			    if (!this.nameDistributions.containsKey(word)) {
				this.nameDistributions.put(word, new NameDistribution());
			    }

			    this.nameDistributions.get(word).bFirstNameCount += bFnPercentage;
			    this.nameDistributions.get(word).bLastNameCount += bLnPercentage;
			    this.nameDistributions.get(word).iFirstNameCount += iFnPercentage;
			    this.nameDistributions.get(word).iLastNameCount += iLnPercentage;
			    this.nameDistributions.get(word).iOtherCount += iOPercentage;
			    this.nameDistributions.get(word).otherCount += oPercentage;
			}
		    }
		}
	    }
	}

    }

    public void printContraints() {
	for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
	    System.out.println(nameEntry.getKey());
	    System.out.println("\tB-FN: " + nameEntry.getValue().bFirstNameCount);
	    System.out.println("\tB-LN: " + nameEntry.getValue().bLastNameCount);
	    System.out.println("\tI-FN: " + nameEntry.getValue().iFirstNameCount);
	    System.out.println("\tI-LN: " + nameEntry.getValue().iLastNameCount);
	    System.out.println("\tI-O: " + nameEntry.getValue().iOtherCount);
	    System.out.println("\tO: " + nameEntry.getValue().otherCount);
	}
    }

    /**
     * stores extracted author statistics in a file according to
     * cc.mallet.fst.semi_supervised.FSTConstraintUtil
     *
     * the file contains lines similar to: Friedrich B-FN:0.2 B-LN:0.1 ...
     *
     * @param outputFile
     * @param bFirstNameLabel
     *            label for beginning first name distribution, is not allowed to
     *            contain colons
     * @param bLastNameLabel
     *            label for beginning last name distribution, is not allowed to
     *            contain colons
     * @param iFirstNameLabel
     *            label for intermediate first name distribution, is not allowed
     *            to contain colons
     * @param iLastNameLabel
     *            label for intermediate last name distribution, is not allowed
     *            to contain colons
     * @param iOtherLabel
     *            label for intermediate other distribution, is not allowed to
     *            contain colons
     * @param otherLabel
     *            label for other distribution, is not allowed to contain colons
     *
     * @throws IOException
     */
    public int writeDistributions(File outputFile, String bFirstNameLabel, String bLastNameLabel,
	    String iFirstNameLabel, String iLastNameLabel, String iOtherLabel, String otherLabel) throws IOException {
	int nameCount = 0;
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
	    String name = nameEntry.getKey();
	    if (name.contains(" ")) {
		bufferedWriter.close();
		throw new IllegalStateException("name contains space: " + name);
	    }

	    nameCount += 1;

	    double bFnPercentage = nameEntry.getValue().bFirstNameCount / nameEntry.getValue().getSum();
	    double bLnPercentage = nameEntry.getValue().bLastNameCount / nameEntry.getValue().getSum();
	    double iFnPercentage = nameEntry.getValue().iFirstNameCount / nameEntry.getValue().getSum();
	    double iLnPercentage = nameEntry.getValue().iLastNameCount / nameEntry.getValue().getSum();
	    double iOtherPercentage = nameEntry.getValue().iOtherCount / nameEntry.getValue().getSum();
	    double otherPercentage = nameEntry.getValue().otherCount / nameEntry.getValue().getSum();
	    String line = name;
	    line += " ";
	    line += bFirstNameLabel + ":" + bFnPercentage;
	    line += " ";
	    line += bLastNameLabel + ":" + bLnPercentage;
	    line += " ";
	    line += iFirstNameLabel + ":" + iFnPercentage;
	    line += " ";
	    line += iLastNameLabel + ":" + iLnPercentage;
	    line += " ";
	    line += iOtherLabel + ":" + iOtherPercentage;
	    line += " ";
	    line += otherLabel + ":" + otherPercentage;

	    line += System.lineSeparator();
	    bufferedWriter.write(line);
	}
	bufferedWriter.close();
	return nameCount;
    }

}
