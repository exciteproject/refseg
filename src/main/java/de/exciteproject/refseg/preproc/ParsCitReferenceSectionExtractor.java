package de.exciteproject.refseg.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.exciteproject.refseg.util.XmlUtils;

/**
 * Class for extracting reference sections from extracted text documents using
 * parsCit.
 */
public class ParsCitReferenceSectionExtractor extends Extractor {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        long startTime = System.currentTimeMillis();
        ParsCitReferenceSectionExtractor referenceSectionExtractor = new ParsCitReferenceSectionExtractor();
        // referenceSectionExtractor.extract(new File(args[0]));
        referenceSectionExtractor.extractInDir(new File(args[0]), new File(args[1]));
        // referenceSectionExtractor.extractReferenceSection(null);
        long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("This took " + (endTime - startTime) + " milliseconds");
    }

    @Override
    public void extract(File inputFile, File outputFile) throws IOException {
        String labeledSections = null;
        try {
            labeledSections = this.labelSections(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // System.out.println(labeledSections);
        String referenceSection = null;
        if (labeledSections != null) {
            try {
                referenceSection = this.extractReferenceSection(labeledSections);
            } catch (ParserConfigurationException | SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (referenceSection != null) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            bufferedWriter.write(referenceSection);
            bufferedWriter.close();

        }
        System.out.println(inputFile.getAbsolutePath());
    }

    public String extractReferenceSection(String labeledSections)
            throws ParserConfigurationException, SAXException, IOException {

        Document document = XmlUtils.loadXMLFromString(labeledSections);
        NodeList referencesParts = document.getElementsByTagName("reference");
        String referenceString = "";
        for (int i = 0, len = referencesParts.getLength(); i < len; i++) {
            referenceString += referencesParts.item(i).getFirstChild().getNodeValue();
        }

        return referenceString;
    }

    /**
     * Labels sections in a research paper using citeExtract.pl from ParsCit
     *
     * @param inputFile
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String labelSections(File inputFile) throws IOException, InterruptedException {

        String[] commandArguments = { "/media/data/masters-thesis/ParsCit/bin/citeExtract.pl", "-m", "extract_section",
                inputFile.getAbsolutePath() };

        Runtime runtime = Runtime.getRuntime();

        Process process = null;
        process = runtime.exec(commandArguments);

        StringWriter writer = new StringWriter();
        IOUtils.copy(process.getInputStream(), writer, "UTF8");

        process.waitFor();

        String labeledSections = writer.toString();

        if (process.exitValue() != 0) {
            throw new IOException("citeExtract.pl exited with exit value: " + process.exitValue());
        }
        return labeledSections;
    }
}
