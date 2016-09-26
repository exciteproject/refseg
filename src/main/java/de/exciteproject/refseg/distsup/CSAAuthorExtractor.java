package de.exciteproject.refseg.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.exciteproject.refseg.preproc.NamePreprocessor;

public class CSAAuthorExtractor extends AuthorExtractor {

    public static void main(String[] args) throws IOException {

	CSAAuthorExtractor csaAuthorExtractor = new CSAAuthorExtractor();
	csaAuthorExtractor.extractAuthorNames(new File(args[0]), new File(args[1]));

    }

    public void extractAuthorNames(File inputDirectory, File outputDirectory) throws IOException {
	this.initializeMaps();

	for (File monthDir : inputDirectory.listFiles()) {
	    for (File ccfFile : monthDir.listFiles()) {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(ccfFile));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
		    if (line.startsWith("AU: ")) {
			line = line.replaceFirst("^AU: ", "");
			String[] authors = line.split("; ");
			for (String author : authors) {
			    String[] authorSplit = author.split(", ");
			    if (authorSplit.length == 2) {
				String lastNames = NamePreprocessor.preprocessName(authorSplit[0]);
				String firstNames = NamePreprocessor.preprocessName(authorSplit[1]);

				this.addNamesToMaps(firstNames, lastNames);
			    }
			}
		    }
		}
		bufferedReader.close();
	    }
	}

	this.writeMaps(outputDirectory);
    }

}
