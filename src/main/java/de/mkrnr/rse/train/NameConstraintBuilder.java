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

public class NameConstraintBuilder {

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
	NameConstraintBuilder nameConstraintBuilder = new NameConstraintBuilder();

	String jsonFileToUsePath = args[0];
	String goddagDictionaryPath = args[1];
	String constraintsOutputFilePath = args[2];
	double unlabeledPercentage = Double.parseDouble(args[3]);
	double bFnPercentage = Double.parseDouble(args[4]);
	double bLnPercentage = Double.parseDouble(args[5]);
	double iFnPercentage = Double.parseDouble(args[6]);
	double iLnPercentage = Double.parseDouble(args[7]);
	double iOPercentage = Double.parseDouble(args[8]);
	double oPercentage = Double.parseDouble(args[9]);
	@SuppressWarnings("unchecked")
	List<File> filesToUse = (List<File>) JsonHelper.readFromFile(new TypeToken<List<File>>() {
	}.getType(), new File(jsonFileToUsePath));
	Set<String> fileIds = new HashSet<String>();
	for (File inputFile : filesToUse) {
	    fileIds.add(FilenameUtils.removeExtension(inputFile.getName()));
	}
	File goddagDirectory = new File(goddagDictionaryPath);
	int count = 0;
	for (File goddagFile : goddagDirectory.listFiles()) {
	    String goddagFileId = FilenameUtils.removeExtension(goddagFile.getName());
	    if (fileIds.contains(goddagFileId)) {
		count++;
		nameConstraintBuilder.extractAuthorStatistics(goddagFile, unlabeledPercentage, bFnPercentage,
			bLnPercentage, iFnPercentage, iLnPercentage, iOPercentage, oPercentage);
	    }
	}
	System.out.println(count);

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

    public void extractAuthorStatistics(File goddagInputFile, double unlabeledPercentage, double bFnPercentage,
	    double bLnPercentage, double iFnPercentage, double iLnPercentage, double iOPercentage, double oPercentage)
	    throws JsonSyntaxException, JsonIOException, FileNotFoundException {
	double totalPercentage = bFnPercentage + bLnPercentage + iFnPercentage + iLnPercentage + iOPercentage
		+ oPercentage;
	if ((totalPercentage - 1.0) != 0) {
	    throw new IllegalArgumentException("percentages don't sum up to one");
	}
	Goddag goddag = this.gson.fromJson(new FileReader(goddagInputFile), Goddag.class);
	GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);

	// iterate over goddag tree to count first names and last names
	for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
	    if (rootChildNode.getLabel().equals(GoddagNameStructure.NodeType.AUTHOR.toString())) {
		boolean isBeginning = true;
		for (Node authorChildNode : rootChildNode.getChildren()) {
		    String nameWord = authorChildNode.getFirstChild().getLabel();
		    if (!this.nameDistributions.containsKey(nameWord)) {
			this.nameDistributions.put(nameWord, new NameDistribution());
		    }
		    if ((authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.FIRST_NAME.toString()))) {
			if (isBeginning) {
			    this.nameDistributions.get(nameWord).bFirstNameCount += 1;
			} else {
			    this.nameDistributions.get(nameWord).iFirstNameCount += 1;
			}
		    } else {
			if (authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.LAST_NAME.toString())) {
			    if (isBeginning) {
				this.nameDistributions.get(nameWord).bLastNameCount += 1;
			    } else {
				this.nameDistributions.get(nameWord).iLastNameCount += 1;
			    }
			}
		    }
		    if (isBeginning) {
			isBeginning = false;
		    }
		}
	    } else {
		if (Math.random() < unlabeledPercentage) {
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

	    double bFirstNamePercentage = nameEntry.getValue().bFirstNameCount / nameEntry.getValue().getSum();
	    double bLastNamePercentage = nameEntry.getValue().bLastNameCount / nameEntry.getValue().getSum();
	    double iFirstNamePercentage = nameEntry.getValue().iFirstNameCount / nameEntry.getValue().getSum();
	    double iLastNamePercentage = nameEntry.getValue().iLastNameCount / nameEntry.getValue().getSum();
	    double iOtherPercentage = nameEntry.getValue().iOtherCount / nameEntry.getValue().getSum();
	    double otherPercentage = nameEntry.getValue().otherCount / nameEntry.getValue().getSum();
	    String line = name;
	    line += " ";
	    line += bFirstNameLabel + ":" + bFirstNamePercentage;
	    line += " ";
	    line += bLastNameLabel + ":" + bLastNamePercentage;
	    line += " ";
	    line += iFirstNameLabel + ":" + iFirstNamePercentage;
	    line += " ";
	    line += iLastNameLabel + ":" + iLastNamePercentage;
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
