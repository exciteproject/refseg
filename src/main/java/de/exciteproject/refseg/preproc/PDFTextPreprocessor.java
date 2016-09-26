package de.exciteproject.refseg.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.exciteproject.refseg.util.FileHelper;

public class PDFTextPreprocessor extends Extractor {

    public static void main(String[] args) throws IOException {

	String inputDirectory = args[0];
	String outputDirectory = args[1];
	PDFTextPreprocessor pdfTextPreprocessor = new PDFTextPreprocessor();
	pdfTextPreprocessor.extractInDir(new File(inputDirectory), new File(outputDirectory));
    }

    @Override
    public void extract(File inputFile, File outputFile) throws IOException {
	String preprocessedText;

	preprocessedText = FileHelper.readFile(inputFile);

	preprocessedText = preprocessedText.replaceAll("/", " / ");

	// TODO handle line breaks, for example when a name gets split with dash

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
