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
public class PDFTextExtractor {

    public static void main(String[] args) throws FileNotFoundException, IOException {

	String inputDirectory = args[1];
	String outputDirectory = args[2];
	PDFTextExtractor pdfTextExtractor = new PDFTextExtractor();
	// String filePath = args[0];
	// String text = pdfTextExtractor.extractText(new File(filePath));
	// System.out.println(text);
	pdfTextExtractor.extractTextFromDirectory(new File(inputDirectory), new File(outputDirectory));
    }

    private Boolean sortByPosition;

    public PDFTextExtractor() {
	this.sortByPosition = null;
    }

    public PDFTextExtractor(boolean sortByPosition) {
	this.sortByPosition = sortByPosition;
    }

    public String extractText(File pdfFile) {
	return this.extractText(pdfFile, 0, 0);
    }

    public String extractText(File pdfFile, int startPage) {
	return this.extractText(pdfFile, startPage, 0);
    }

    public String extractText(File pdfFile, int startPage, int endPage) {
	PDDocument pdDocument = null;
	try {
	    PDFParser pdfParser = new PDFParser(new RandomAccessFile(pdfFile, "r"));
	    pdfParser.parse();

	    COSDocument cosDocument = pdfParser.getDocument();

	    pdDocument = new PDDocument(cosDocument);

	    PDFTextStripper pdfTextStripper = new PDFTextStripper();

	    pdfTextStripper.setAddMoreFormatting(true);

	    if (startPage > 0) {
		pdfTextStripper.setStartPage(startPage);
	    }

	    if (endPage > 0) {
		pdfTextStripper.setEndPage(endPage);
	    } else {
		pdfTextStripper.setEndPage(pdDocument.getNumberOfPages());
	    }

	    if (this.sortByPosition != null) {
		pdfTextStripper.setSortByPosition(this.sortByPosition);
	    }

	    String text = pdfTextStripper.getText(pdDocument);

	    return text;

	} catch (FileNotFoundException e) {
	    return null;
	} catch (IOException e) {
	    return null;
	} finally {
	    if (pdDocument != null) {
		try {

		    pdDocument.close();
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	}
    }

    public void extractTextFromDirectory(File inputDirectory, File outputDirectory) throws FileNotFoundException {
	if (!inputDirectory.isDirectory()) {
	    throw new IllegalArgumentException("inputDirectory is not a directory");
	}

	if (!outputDirectory.exists()) {
	    outputDirectory.mkdirs();
	}

	try {
	    for (File inputFile : inputDirectory.listFiles()) {
		// TODO: set 1 with parameters
		String currentPDF = this.extractText(inputFile, 2);
		String outputFileName = inputFile.getName().replaceAll("\\.pdf", ".txt");
		File outputFile = new File(outputDirectory.getAbsolutePath() + File.separator + outputFileName);

		if (currentPDF != null) {
		    try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
			bufferedWriter.write(currentPDF);
			bufferedWriter.close();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	} catch (NullPointerException e) {
	    throw new FileNotFoundException();
	}
    }

}
