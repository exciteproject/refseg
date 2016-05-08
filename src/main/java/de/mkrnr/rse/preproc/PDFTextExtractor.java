package de.mkrnr.rse.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * A class for extracting text from PDF files using Apache PDFBox.
 */
public class PDFTextExtractor extends Extractor {

    public static void main(String[] args) throws FileNotFoundException, IOException {

	String inputDirectory = args[1];
	String outputDirectory = args[2];
	PDFTextExtractor pdfTextExtractor = new PDFTextExtractor(2);
	// String filePath = args[0];
	// String text = pdfTextExtractor.extractText(new File(filePath));
	// System.out.println(text);
	pdfTextExtractor.extractInDir(new File(inputDirectory), new File(outputDirectory));
    }

    private Boolean sortByPosition;
    private int startPage;
    private int endPage;

    public PDFTextExtractor(boolean sortByPosition) {
	this.sortByPosition = sortByPosition;
    }

    public PDFTextExtractor(int startPage) {
	this.startPage = startPage;
	this.endPage = -1;
	this.sortByPosition = null;
    }

    @Override
    public void extract(File inputFile, File outputFile) {
	PDDocument pdDocument = null;
	try {
	    PDFParser pdfParser = new PDFParser(new RandomAccessFile(inputFile, "r"));
	    pdfParser.parse();

	    COSDocument cosDocument = pdfParser.getDocument();

	    pdDocument = new PDDocument(cosDocument);

	    PDFTextStripper pdfTextStripper = new PDFTextStripper();

	    pdfTextStripper.setAddMoreFormatting(true);

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

	    String text = pdfTextStripper.getText(pdDocument);

	    String outputFileName = outputFile.getName().replaceAll("\\.pdf", ".txt");
	    outputFile = new File(outputFile.getParentFile().getAbsolutePath() + File.separator + outputFileName);

	    if (text.length() > 0) {
		try {
		    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
		    bufferedWriter.write(text);
		    bufferedWriter.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    if (pdDocument != null) {
		try {
		    pdDocument.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

}
