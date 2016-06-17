package de.mkrnr.rse.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;
import de.mkrnr.rse.distsup.GoddagNameStructure;

public class NameConstraintBuilder {

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
	NameConstraintBuilder nameConstraintBuilder = new NameConstraintBuilder();
	File goddagDirectory = new File(args[0]);
	for (File goddagFile : goddagDirectory.listFiles()) {
	    nameConstraintBuilder.extractAuthorStatistics(goddagFile);
	}

	// nameConstraintBuilder.printContraints();
	nameConstraintBuilder.writeDistributions(new File(args[1]), "B-FN", "I-FN", "B-LN", "I-LN");
    }

    private Gson gson;

    private Map<String, NameDistribution> nameDistributions;

    public NameConstraintBuilder() {
	GsonBuilder gsonBuilder = new GsonBuilder();
	gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
	this.gson = gsonBuilder.create();

	this.nameDistributions = new HashMap<String, NameDistribution>();

    }

    public void extractAuthorStatistics(File goddagInputFile)
	    throws JsonSyntaxException, JsonIOException, FileNotFoundException {
	Goddag goddag = this.gson.fromJson(new FileReader(goddagInputFile), Goddag.class);
	GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);

	// iterate over goddag tree to count first names and last names
	for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
	    if (rootChildNode.getLabel().equals(GoddagNameStructure.NodeType.AUTHOR.toString())) {
		boolean isBeginning = true;
		for (Node authorChildNode : rootChildNode.getChildren()) {
		    if ((authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.FIRST_NAME.toString()))
			    || (authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.LAST_NAME.toString()))) {

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
				this.nameDistributions.get(nameWord).bLastNameCount += 1;
			    } else {
				this.nameDistributions.get(nameWord).iLastNameCount += 1;
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

    public void printContraints() {
	for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
	    System.out.println(nameEntry.getKey());
	    System.out.println("\tB-FN: " + nameEntry.getValue().bFirstNameCount);
	    System.out.println("\tI-FN: " + nameEntry.getValue().iFirstNameCount);
	    System.out.println("\tB-LN: " + nameEntry.getValue().bLastNameCount);
	    System.out.println("\tI-LN: " + nameEntry.getValue().iLastNameCount);
	}
    }

    /**
     * stores extracted author statistics in a file according to
     * cc.mallet.fst.semi_supervised.FSTConstraintUtil
     *
     * the file contains lines following this example: Friedrich fn:0.4 ln:0.6
     *
     * @param outputFile
     * @param bFirstNameLabel
     *            label for beginning first name distribution, is not allowed to
     *            contain colons
     * @param iFirstNameLabel
     *            label for intermediate first name distribution, is not allowed
     *            to contain colons
     * @param bLastNameLabel
     *            label for beginning last name distribution, is not allowed to
     *            contain colons
     * @param iLastNameLabel
     *            label for intermediate last name distribution, is not allowed
     *            to contain colons
     *
     * @throws IOException
     */
    public void writeDistributions(File outputFile, String bFirstNameLabel, String iFirstNameLabel,
	    String bLastNameLabel, String iLastNameLabel) throws IOException {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
	    String name = nameEntry.getKey();
	    if (name.contains(" ")) {
		bufferedWriter.close();
		throw new IllegalStateException("name contains space: " + name);
	    }

	    double bFirstNamePercentage = (double) nameEntry.getValue().bFirstNameCount / nameEntry.getValue().getSum();
	    double iFirstNamePercentage = (double) nameEntry.getValue().iFirstNameCount / nameEntry.getValue().getSum();
	    double bLastNamePercentage = (double) nameEntry.getValue().bLastNameCount / nameEntry.getValue().getSum();
	    double iLastNamePercentage = (double) nameEntry.getValue().iLastNameCount / nameEntry.getValue().getSum();
	    String line = name;
	    line += " ";
	    line += bFirstNameLabel + ":" + bFirstNamePercentage;
	    line += " ";
	    line += iFirstNameLabel + ":" + iFirstNamePercentage;
	    line += " ";
	    line += bLastNameLabel + ":" + bLastNamePercentage;
	    line += " ";
	    line += iLastNameLabel + ":" + iLastNamePercentage;

	    // TODO parameterize
	    line += " ";
	    line += "O" + ":" + "0.0";

	    line += System.lineSeparator();
	    bufferedWriter.write(line);
	}
	bufferedWriter.close();

    }

}
