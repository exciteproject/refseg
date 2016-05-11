package de.mkrnr.rse.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * A class for extracting text from PDF files using Apache PDFBox.
 */
public class PDFTextExtractor extends Extractor {

    public static void main(String[] args) throws IOException {

	String inputDirectory = args[0];
	String outputDirectory = args[1];
	PDFTextExtractor pdfTextExtractor = new PDFTextExtractor(2, null, null);
	// String filePath = args[0];
	// String text = pdfTextExtractor.extractText(new File(filePath));
	// System.out.println(text);
	// pdfTextExtractor.extract(new File(inputDirectory), new
	// File(outputDirectory));
	inputDirectory = "/media/data/masters-thesis/papers/33978.pdf";
	outputDirectory = "/media/data/masters-thesis/papers/33978.pdf";
	pdfTextExtractor.extract(new File(inputDirectory), new File(outputDirectory));
    }

    private Boolean sortByPosition;
    private int startPage;
    private int endPage;
    private Boolean addMoreFormatting;

    public PDFTextExtractor(int startPage, Boolean sortByPosition, Boolean addMoreFormatting) {
	this.startPage = startPage;
	this.endPage = 0;
	this.sortByPosition = sortByPosition;
	this.addMoreFormatting = addMoreFormatting;
    }

    @Override
    public void extract(File inputFile, File outputFile) throws IOException {
	System.out.println(inputFile.getName());
	RandomAccessFile randomAccessFile = null;
	PDDocument pdDocument = null;
	BufferedWriter bufferedWriter = null;
	try {
	    randomAccessFile = new RandomAccessFile(inputFile, "r");
	    PDFParser pdfParser = new PDFParser(randomAccessFile);
	    pdfParser.parse();

	    pdDocument = pdfParser.getPDDocument();

	    PDFTextStripper pdfTextStripper = new PDFTextStripper();

	    if (this.startPage > 0) {
		pdfTextStripper.setStartPage(this.startPage);
	    }

	    if (this.endPage > 0) {
		pdfTextStripper.setEndPage(this.endPage);
	    } else {
		pdfTextStripper.setEndPage(pdDocument.getNumberOfPages());
	    }

	    if (this.sortByPosition != null) {
		pdfTextStripper.setSortByPosition(this.sortByPosition);
	    }
	    if (this.addMoreFormatting != null) {
		pdfTextStripper.setAddMoreFormatting(true);
	    }

	    String text = pdfTextStripper.getText(pdDocument);

	    String outputFileName = outputFile.getName().replaceAll("\\.pdf", ".txt");
	    outputFile = new File(outputFile.getParentFile().getAbsolutePath() + File.separator + outputFileName);

	    if (text.length() > 0) {
		bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
		bufferedWriter.write(text);
	    }

	} catch (ClassCastException e) {
	    // thrown e.g. by 40259.pdf

	    e.printStackTrace();
	} catch (IllegalArgumentException e) {
	    // thrown e.g. by 33978.pdf
	    e.printStackTrace();
	} finally {
	    if (randomAccessFile != null) {
		randomAccessFile.close();
	    }
	    if (pdDocument != null) {
		pdDocument.close();
	    }
	    if (bufferedWriter != null) {
		bufferedWriter.close();
	    }
	}
    }
}
