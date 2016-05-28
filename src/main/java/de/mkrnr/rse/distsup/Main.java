package de.mkrnr.rse.distsup;

import java.io.File;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import de.mkrnr.rse.distsup.GoddagNameStructure.NodeType;

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

    @Parameter(names = { "-tagged",
	    "--tagged-dir" }, description = "directory in which the tagged files are stored", converter = FileConverter.class)
    private File taggedDirectory;

    @Parameter(names = { "-matched",
	    "--matched-dir" }, description = "directory in which the matched files are stored", converter = FileConverter.class)
    private File matchedDirectory;

    @Parameter(names = { "-names",
	    "--names-file" }, description = "file listing names", converter = FileConverter.class)
    private File namesFile;

    @Parameter(names = { "-first-names",
	    "--first-names-file" }, description = "file listing first names with counts", converter = FileConverter.class)
    private File firstNamesFile;

    @Parameter(names = { "-last-names",
	    "--last-names-file" }, description = "file listing last names with counts", converter = FileConverter.class)
    private File lastNamesFile;

    private void run() throws IOException {
	if ((this.inputDirectory != null) && (this.taggedDirectory != null) && (this.matchedDirectory != null)) {
	    NameTagger nameTagger = new NameTagger(true);

	    nameTagger.createNameMap(this.firstNamesFile, true, NodeType.FIRST_NAME.toString());
	    nameTagger.createNameMap(this.lastNamesFile, false, NodeType.LAST_NAME.toString());
	    nameTagger.tagDirectory(this.inputDirectory, this.taggedDirectory);

	    NameMatcher nameMatcher = new NameMatcher(this.namesFile, NodeType.FIRST_NAME.toString(),
		    NodeType.LAST_NAME.toString(), NodeType.AUTHOR.toString());
	    nameMatcher.matchDirectory(this.taggedDirectory, this.matchedDirectory);

	}

    }
}
