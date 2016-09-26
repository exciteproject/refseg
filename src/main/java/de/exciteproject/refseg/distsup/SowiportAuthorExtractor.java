package de.exciteproject.refseg.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SowiportAuthorExtractor extends AuthorExtractor {

    public static void main(String[] args) throws IOException {
	File inputFile = new File(args[0]);
	File outputDirectory = new File(args[1]);
	int maxNumberOfNames = Integer.parseInt(args[2]);

	SowiportAuthorExtractor sowiportAuthorExtractor = new SowiportAuthorExtractor();
	List<String> nameStringList = sowiportAuthorExtractor.extractAuthorNames(inputFile);

	sowiportAuthorExtractor.addNameStringListToMaps(nameStringList, maxNumberOfNames);
	sowiportAuthorExtractor.writeMaps(outputDirectory);
    }

    public int count = 0;

    public List<String> extractAuthorNames(File inputFile) throws IOException {
	List<String> nameStringList = new ArrayList<String>();

	BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
	String line;
	while ((line = bufferedReader.readLine()) != null) {
	    if (line.startsWith("      <str>")) {
		if (line.matches(".*&[a-z]+;.*")) {
		    continue;
		}
		line = line.replaceFirst("^      <str>", "");
		line = line.replaceFirst("</str>$", "");

		line = this.preprocessLine(line);

		if (line.contains(";")) {
		    String[] authors = line.split(";");
		    for (String author : authors) {
			this.addNameStringToList(author, nameStringList);
		    }
		} else {
		    this.addNameStringToList(line, nameStringList);
		}
	    }
	}
	bufferedReader.close();

	return nameStringList;
    }

    private void addNameStringToList(String nameString, List<String> nameStringList) {
	String[] nameSplit = nameString.split(", ");
	if (nameSplit.length == 2) {
	    nameStringList.add(nameString);
	}
    }
}
