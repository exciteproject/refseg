package de.mkrnr.rse.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import de.mkrnr.rse.util.FileHelper;

public class PDFTextPreprocessor {

    public static void main(String[] args) throws FileNotFoundException {

	String inputDirectory = args[0];
	String outputDirectory = args[1];
	PDFTextPreprocessor pdfTextPreprocessor = new PDFTextPreprocessor();
	pdfTextPreprocessor.preprocessTextInDirecotry(new File(inputDirectory), new File(outputDirectory));
    }

    public void preprocessTextInDirecotry(File inputDirectory, File outputDirectory) {
	if (!inputDirectory.isDirectory()) {
	    throw new IllegalArgumentException("inputDirectory is not a directory");
	}

	if (!outputDirectory.exists()) {
	    outputDirectory.mkdirs();
	}

	for (File inputFile : inputDirectory.listFiles()) {
	    String preprocessedText = this.preprocessText(inputFile);
	    String outputFileName = inputFile.getName();
	    File outputFile = new File(outputDirectory.getAbsolutePath() + File.separator + outputFileName);

	    if (preprocessedText != null) {
		try {
		    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
		    bufferedWriter.write(preprocessedText);
		    bufferedWriter.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}

    }

    private String preprocessText(File inputFile) {
	if (!inputFile.exists() || !inputFile.isFile()) {
	    return null;
	}

	String preprocessedText;
	try {
	    preprocessedText = FileHelper.readFile(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}

	preprocessedText = preprocessedText.replaceAll("/", " / ");

	// TODO handle line breaks, for example when a name gets split with dash

	return preprocessedText;
    }

}
