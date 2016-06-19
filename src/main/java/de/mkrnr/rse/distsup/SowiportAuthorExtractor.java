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
		line = line.replaceFirst("^      <str>", "");
		line = line.replaceFirst("</str>$", "");
		String[] authorSplit = line.split(", ");
		if (authorSplit.length == 2) {
		    String lastNames = NamePreprocessor.preprocessName(authorSplit[0]);
		    String firstNames = NamePreprocessor.preprocessName(authorSplit[1]);

		    this.addNamesToMaps(firstNames, lastNames);
		}
	    }
	}
	bufferedReader.close();

	this.writeMaps(outputDirectory);
    }

}
