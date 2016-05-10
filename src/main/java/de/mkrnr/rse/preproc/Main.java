package de.mkrnr.rse.preproc;

import java.io.File;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

public class Main {

    public static void main(String[] args) {
	Main main = new Main();

	JCommander jCommander;
	try {
	    jCommander = new JCommander(main, args);
	} catch (ParameterException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	    return;
	}

	if (main.help) {
	    jCommander.usage();
	} else {
	    main.run();
	}
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters")
    private boolean help;

    @Parameter(names = { "-pdf",
	    "--pdf-dir" }, description = "directory containing pdf research papers", converter = FileConverter.class)
    private File pdfDirectory;

    @Parameter(names = { "-pdf-start", "--pdf-start-page" }, description = "start page for pdf extraction")
    private Integer pdfStartPage = 1;

    @Parameter(names = { "-text",
	    "--text-dir" }, description = "directory in which the extracted text files are stored", converter = FileConverter.class)
    private File textDirectory;

    @Parameter(names = { "-preproc",
	    "--preprocessed-dir" }, description = "directory in which the preprocessed text files are stored", converter = FileConverter.class)
    private File preProcDirectory;

    private void run() {
	if (this.pdfDirectory != null) {
	    if (this.textDirectory != null) {
		// TODO Add parameters for sortByPosition and addMoreFormatting
		PDFTextExtractor pdfTextExtractor = new PDFTextExtractor(this.pdfStartPage, null, null);
		pdfTextExtractor.extractInDir(this.pdfDirectory, this.textDirectory);
	    } else {
		throw new ParameterException("\"-text\" needs to be set as output when \"-pdf\" is set");
	    }
	}

	if (this.textDirectory != null) {
	    if (this.preProcDirectory != null) {
		PDFTextPreprocessor pdfTextPreprocessor = new PDFTextPreprocessor();
		pdfTextPreprocessor.extractInDir(this.textDirectory, this.preProcDirectory);
	    } else {
		throw new ParameterException("\"-preproc\" needs to be set as output when \"-text\" is set");
	    }
	}
    }
}
