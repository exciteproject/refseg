package de.exciteproject.refseg.preproc;

import java.io.File;
import java.io.IOException;

public abstract class Extractor {

    public abstract void extract(File inputFile, File outputFile) throws IOException;

    public void extractInDir(File inputDir, File outputDir) {
	outputDir.mkdirs();

	File[] inputDirFiles = null;
	if (inputDir != null) {
	    inputDirFiles = inputDir.listFiles();
	}

	if (inputDirFiles != null) {
	    for (File inputFile : inputDirFiles) {
		try {
		    this.extract(inputFile,
			    new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
		} catch (IOException e) {
		    System.err.print("IOException during extraction of file " + inputFile.getName() + ": ");
		    System.err.println(e.getMessage());
		}
	    }
	}
    }
}
