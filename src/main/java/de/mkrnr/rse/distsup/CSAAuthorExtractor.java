package de.mkrnr.rse.distsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class CSAAuthorExtractor extends AuthorExtractor {

    public static void main(String[] args) throws FileNotFoundException {

	CSAAuthorExtractor csaAuthorExtractor = new CSAAuthorExtractor();
	csaAuthorExtractor.extractAuthorNames(new File(args[0]), new File(args[1]), new File(args[2]),
		new File(args[3]));

    }

    public void extractAuthorNames(File inputDirectory, File forenameOutputFile, File surnameOutputFile,
	    File nameOutputFile) throws FileNotFoundException {
	HashMap<String, Integer> forenameMap = new HashMap<String, Integer>();
	HashMap<String, Integer> surnameMap = new HashMap<String, Integer>();
	HashMap<String, Integer> nameMap = new HashMap<String, Integer>();

	try {
	    for (File monthDir : inputDirectory.listFiles()) {
		for (File ccfFile : monthDir.listFiles()) {
		    try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(ccfFile));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
			    if (line.startsWith("AU: ")) {
				line = line.replaceFirst("^AU: ", "");
				String[] authors = line.split("; ");
				for (String author : authors) {
				    String[] authorSplit = author.split(", ");
				    if (authorSplit.length == 2) {
					String surname = authorSplit[0];
					String forename = authorSplit[1];

					this.addNamesToMap(forename, forenameMap);
					this.addNamesToMap(surname, surnameMap);
					this.addNamesToMap(forename + "\t" + surname.toString(), nameMap);
				    }
				}
			    }
			}
			bufferedReader.close();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		}

	    }
	} catch (NullPointerException e) {
	    throw new FileNotFoundException();
	}
	this.writeMapToFile(forenameMap, forenameOutputFile);
	this.writeMapToFile(surnameMap, surnameOutputFile);
	this.writeMapToFile(nameMap, nameOutputFile);

    }

}
