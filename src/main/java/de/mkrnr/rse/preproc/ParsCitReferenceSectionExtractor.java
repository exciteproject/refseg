package de.mkrnr.rse.preproc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.mkrnr.rse.util.FileHelper;
import de.mkrnr.rse.util.XMLHelper;

public class ParsCitReferenceSectionExtractor {

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

	long startTime = System.currentTimeMillis();
	ParsCitReferenceSectionExtractor referenceSectionExtractor = new ParsCitReferenceSectionExtractor();
	// referenceSectionExtractor.extract(new File(args[0]));
	referenceSectionExtractor.extractReferenceSection(null);
	long endTime = System.currentTimeMillis();
	System.out.println();
	System.out.println("This took " + (endTime - startTime) + " milliseconds");
    }

    public String extract(File inputFile) throws ParserConfigurationException, SAXException, IOException {
	String labeledSections;
	try {
	    labeledSections = this.labelSections(inputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	}

	System.out.println(labeledSections);
	String referenceSection = this.extractReferenceSection(labeledSections);
	return referenceSection;
    }

    public String extractReferenceSection(String labeledSections)
	    throws ParserConfigurationException, SAXException, IOException {

	labeledSections = FileHelper.readFile(new File("/media/data/masters-thesis/papers/test.xml"));
	Document document = XMLHelper.loadXMLFromString(labeledSections);
	NodeList referencesParts = document.getElementsByTagName("reference");
	for (int i = 0, len = referencesParts.getLength(); i < len; i++) {
	    System.out.println(referencesParts.item(i).getFirstChild().getNodeValue());
	}

	return null;
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
