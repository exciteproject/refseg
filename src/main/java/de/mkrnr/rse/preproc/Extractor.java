package de.mkrnr.rse.preproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class Extractor {

    public abstract void extract(File inputFile, File outputFile) throws IOException;

    public void extractInDir(File inputDir, File outputDir) throws IOException {
	outputDir.mkdirs();

	try {
	    for (File inputFile : inputDir.listFiles()) {
		this.extract(inputFile, new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
	    }
	} catch (NullPointerException e) {
	    throw new FileNotFoundException();
	}
    }
}
