package de.mkrnr.rse.stat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;
import de.mkrnr.rse.distsup.GoddagNameStructure;

public class GoddagNameCounter {

    public static void countNames(File goddagDir) throws JsonSyntaxException, JsonIOException, IOException {
	GsonBuilder gsonBuilder = new GsonBuilder();
	gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
	Gson gson = gsonBuilder.create();
	int firstNameCount = 0;
	int lastNameCount = 0;
	int authorCount = 0;
	for (File goddagFile : goddagDir.listFiles()) {

	    FileReader fileReader = new FileReader(goddagFile);
	    Goddag goddag = gson.fromJson(fileReader, Goddag.class);
	    GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);
	    for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
		if ("[Author]".equals(rootChildNode.getLabel())) {
		    authorCount++;
		    for (Node authorChildNode : rootChildNode.getChildren()) {
			switch (authorChildNode.getLabel()) {
			case "[FirstName]":
			    firstNameCount++;
			    break;
			case "[LastName]":
			    lastNameCount++;
			    break;
			}
		    }
		}
	    }
	    fileReader.close();
	}
	System.out.println(goddagDir.getAbsolutePath());
	System.out.println("authorCount: " + authorCount);
	System.out.println("firstNameCount: " + firstNameCount);
	System.out.println("lastNameCount: " + lastNameCount);
    }

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
	GoddagNameCounter.countNames(new File(args[0]));
    }
}
