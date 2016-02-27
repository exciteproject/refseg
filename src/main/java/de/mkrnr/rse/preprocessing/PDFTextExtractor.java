package de.mkrnr.rse.preprocessing;

import java.io.File;
import java.io.FileNotFoundException;
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
        String filePath = args[0];

        PDFTextExtractor pdfTextExtractor = new PDFTextExtractor();
        String text = pdfTextExtractor.extractText(new File(filePath));
        System.out.println(text);
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

            pdDocument.close();

            return text;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
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

}
