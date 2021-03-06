package de.exciteproject.refseg.distsup;

import java.io.File;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

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
        // TODO add
    }
}
