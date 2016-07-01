package de.mkrnr.rse.inst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import de.mkrnr.rse.util.JsonHelper;
import de.mkrnr.rse.util.ListHelper;

public class Main {

    public static void main(String[] args) throws IOException {
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

    @Parameter(names = { "-input",
	    "--input-dir" }, description = "directory containing text files", converter = FileConverter.class)
    private File inputDirectory;

    @Parameter(names = { "-output",
	    "--output-file" }, description = "file in which the instances will be stored", converter = FileConverter.class)
    private File outputFile;

    @Parameter(names = { "-filter",
	    "--filter-dir" }, description = "directory containing text files that should be removed from the input file list", converter = FileConverter.class)
    private File filterDirectory;

    @Parameter(names = { "-labels",
	    "--label-list" }, description = "comma separated list of labels that are randomly added", variableArity = true, required = true)
    private List<String> labels;

    @Parameter(names = { "-create", "--create-instances" }, description = "creates random instances if true")
    private boolean createInstances = false;

    @Parameter(names = { "-merge", "--merge-instances" }, description = "merges instance files if true")
    private boolean mergeInstances = false;

    private void run() throws IOException {
	List<File> filesToRemove = new ArrayList<File>();
	if (this.filterDirectory != null) {
	    filesToRemove = Arrays.asList(this.filterDirectory.listFiles());
	}
	if ((this.inputDirectory != null) && (this.outputFile != null)) {

	    List<File> inputFiles = Arrays.asList(this.inputDirectory.listFiles());
	    List<File> filteredFiles = ListHelper.removeFilesFromList(inputFiles, filesToRemove);

	    if (this.createInstances) {
		if (this.labels == null) {
		    throw new IllegalArgumentException(
			    "labels need to be specified when creating a random instance list");
		}
		RandomInstanceFileBuilder.createInstanceFile(filteredFiles, this.outputFile, this.labels);
	    }
	    if (this.mergeInstances) {
		RandomInstanceFileBuilder.mergeInstanceFiles(filteredFiles, this.outputFile);
	    }

	    File outputFilesFile = new File(
		    FilenameUtils.removeExtension(this.outputFile.getAbsolutePath()) + "_files.json");
	    JsonHelper.writeToFile(filteredFiles, outputFilesFile);
	}
    }
}
