package de.exciteproject.refseg.distsup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.exciteproject.refseg.util.CsvUtils;
import de.exciteproject.refseg.util.FileUtils;
import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

/**
 * Matches, for a given file or directory of files, reference strings against
 * different data sets to assign labels by creating GODDAG graphs
 *
 * @author mkoerner
 *
 */
public class ReferenceMatcher {

    public static void main(String[] args) throws IOException {
        ReferenceMatcher referenceMatcher = new ReferenceMatcher();

        JCommander jCommander;
        try {
            jCommander = new JCommander(referenceMatcher, args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        if (referenceMatcher.help) {
            jCommander.usage();
        } else {
            referenceMatcher.run();
        }
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters")
    private boolean help;

    @Parameter(names = { "-refs",
            "--reference-directory" }, description = "directory containing text files where each line is a reference string", converter = FileConverter.class, required = true)
    private File referenceDirectory;
    @Parameter(names = { "-out",
            "--output-directory" }, description = "directory for the tagged goddag files", converter = FileConverter.class, required = true)
    private File outputDirectory;

    @Parameter(names = { "-authors",
            "--authors-file" }, description = "file listing names where given names and surenames are separated by a tab, including a tab separated count", converter = FileConverter.class)
    private File authorsFile;

    @Parameter(names = { "-titles",
            "--titles-file" }, description = "file listing titles, including a tab separated count", converter = FileConverter.class)
    private File titlesFile;

    private Gson gson;

    public ReferenceMatcher() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonSerializer());
        gsonBuilder.registerTypeAdapter(Node.class, Node.getJsonSerializer());
        this.gson = gsonBuilder.create();
    }

    private void run() throws IOException {
        WordNormalizer wordNormalizer = new StartEndWordNormalizer();
        String wordSplitRegex = "\\s";
        String columnSeparator = "\t";

        List<Tagger> taggers = new ArrayList<Tagger>();

        if (this.authorsFile != null) {
            NameTagger authorTagger = new NameTagger("[A]", "[FN]", "[LN]", wordNormalizer);

            String authorsString = FileUtils.readFile(this.authorsFile);
            List<String> givenNames = CsvUtils.readColumn(0, authorsString, columnSeparator);
            List<String> surNames = CsvUtils.readColumn(1, authorsString, columnSeparator);
            List<Name> authorNames = new ArrayList<Name>();
            for (int i = 0; i < givenNames.size(); i++) {
                authorNames.add(new Name(givenNames.get(i), surNames.get(i)));
            }
            authorTagger.readAuthors(authorNames, true);

            taggers.add(authorTagger);

        }
        if (this.titlesFile != null) {
            StringTagger titleTagger = new StringTagger("[T]", wordSplitRegex, wordNormalizer);

            String titlesString = FileUtils.readFile(this.titlesFile);
            List<String> titles = CsvUtils.readColumn(0, titlesString, columnSeparator);
            titleTagger.readStrings(titles);

            taggers.add(titleTagger);

        }
        GoddagBuilder goddagBuilder = new GoddagBuilder("[R]", wordSplitRegex);
        for (File referenceFile : this.referenceDirectory.listFiles()) {

            String[] references = FileUtils.readFile(referenceFile).split("\n");

            List<Goddag> goddags = new ArrayList<Goddag>();
            for (String reference : references) {
                Goddag goddag = goddagBuilder.build(reference);

                for (Tagger tagger : taggers) {
                    tagger.tag(goddag);
                }
                System.out.println(goddag);

                goddags.add(goddag);

            }
            String outputFileName = this.outputDirectory.getAbsolutePath() + "/"
                    + FilenameUtils.removeExtension(referenceFile.getName()) + ".json";

            org.apache.commons.io.FileUtils.writeStringToFile(new File(outputFileName),
                    this.gson.toJson(goddags, ArrayList.class));

        }
    }
}
