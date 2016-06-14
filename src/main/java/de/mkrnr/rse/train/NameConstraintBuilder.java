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
	nameConstraintBuilder.writeDistributions(new File(args[1]), "fn", "ln");
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
		for (Node authorChildNode : rootChildNode.getChildren()) {
		    if ((authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.FIRST_NAME.toString()))
			    || (authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.LAST_NAME.toString()))) {
			// TODO set word property instead of hardcoding it?
			String nameWord = authorChildNode.getFirstChild().getProperty("word");
			if (!this.nameDistributions.containsKey(nameWord)) {
			    this.nameDistributions.put(nameWord, new NameDistribution());
			}

			if ((authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.FIRST_NAME.toString()))) {
			    this.nameDistributions.get(nameWord).addToFirstNameCount(1);
			} else {
			    if (authorChildNode.getLabel().equals(GoddagNameStructure.NodeType.LAST_NAME.toString())) {
				this.nameDistributions.get(nameWord).addToLastNameCount(1);
			    }
			}
		    }
		}
	    }
	}
    }

    public void printContraints() {
	for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
	    System.out.println(nameEntry.getKey());
	    System.out.println("\tfirstName: " + nameEntry.getValue().getFirstNamePercentage());
	    System.out.println("\tlastName: " + nameEntry.getValue().getLastNamePercentage());
	}
    }

    /**
     * stores extracted author statistics in a file according to
     * cc.mallet.fst.semi_supervised.FSTConstraintUtil
     *
     * the file contains lines following this example: Friedrich fn:0.4 ln:0.6
     *
     * @param outputFile
     * @param firstNameLabel
     *            label for first name distribution, is not allowed to contain
     *            colons
     * @param lastNameLabel
     *            label for last name distribution, is not allowed to contain
     *            colons
     *
     * @throws IOException
     */
    public void writeDistributions(File outputFile, String firstNameLabel, String lastNameLabel) throws IOException {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
	for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
	    String name = nameEntry.getKey();
	    if (name.contains(" ")) {
		bufferedWriter.close();
		throw new IllegalStateException("name contains space: " + name);
	    }
	    String line = name;
	    line += " ";
	    line += firstNameLabel + ":" + nameEntry.getValue().getFirstNamePercentage();
	    line += " ";
	    line += lastNameLabel + ":" + nameEntry.getValue().getLastNamePercentage();
	    line += System.lineSeparator();
	    bufferedWriter.write(line);
	}
	bufferedWriter.close();

    }

}
