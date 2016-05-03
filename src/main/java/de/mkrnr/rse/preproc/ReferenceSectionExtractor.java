package de.mkrnr.rse.preproc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class ReferenceSectionExtractor {

    public static void main(String[] args) {

	long startTime = System.currentTimeMillis();
	ReferenceSectionExtractor referenceSectionExtractor = new ReferenceSectionExtractor();
	referenceSectionExtractor.extract(new File(args[0]));
	long endTime = System.currentTimeMillis();
	System.out.println();
	System.out.println("This took " + (endTime - startTime) + " milliseconds");
    }

    public String extract(File inputFile) {
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
	String referenceSection = null;
	return referenceSection;
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
