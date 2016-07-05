package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.mkrnr.rse.preproc.NamePreprocessor;

public class SowiportAuthorExtractor extends AuthorExtractor {

    public static void main(String[] args) throws IOException {

	SowiportAuthorExtractor sowiportAuthorExtractor = new SowiportAuthorExtractor();
	sowiportAuthorExtractor.extractAuthorNames(new File(args[0]), new File(args[1]));

    }

    public void extractAuthorNames(File inputFile, File outputDirectory) throws IOException {
	this.initializeMaps();

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
			this.addAuthor(author);
		    }
		} else {
		    this.addAuthor(line);
		}
	    }
	}
	bufferedReader.close();

	this.writeMaps(outputDirectory);
    }

    private void addAuthor(String nameString) {
	String[] nameSplit = nameString.split(", ");
	if (nameSplit.length == 2) {
	    String lastNames = NamePreprocessor.preprocessName(nameSplit[0]);
	    String firstNames = NamePreprocessor.preprocessName(nameSplit[1]);
	    this.addNamesToMaps(firstNames, lastNames);
	}
    }
}
