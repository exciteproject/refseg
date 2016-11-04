package de.exciteproject.refseg.distsup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.exciteproject.refseg.util.CsvUtils;
import de.exciteproject.refseg.util.FileUtils;
import de.exciteproject.refseg.util.GoddagUtils;
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

    @Parameter(names = { "-publisher-locs",
            "--publisher-localizations-file" }, description = "file listing publisher localizations, including a tab separated count", converter = FileConverter.class)
    private File publisherLocsFile;

    @Parameter(names = { "-publisher-names",
            "--publisher-names-file" }, description = "file listing publisher names, including a tab separated count", converter = FileConverter.class)
    private File publisherNamesFile;

    @Parameter(names = { "-sources",
            "--sources" }, description = "file listing sources, including a tab separated count", converter = FileConverter.class)
    private File sourcesFile;

    @Parameter(names = { "-titles",
            "--titles-file" }, description = "file listing titles, including a tab separated count", converter = FileConverter.class)
    private File titlesFile;

    @Parameter(names = { "-years",
            "--years-file" }, description = "file listing years, including a tab separated count", converter = FileConverter.class)
    private File yearsFile;

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

        Map<File, String> fileLabelMap = new HashMap<File, String>();
        if (this.publisherLocsFile != null) {
            // TODO set labels via parameters
            fileLabelMap.put(this.publisherLocsFile, "PL");
        }
        if (this.publisherNamesFile != null) {
            fileLabelMap.put(this.publisherNamesFile, "PN");
        }
        if (this.sourcesFile != null) {
            fileLabelMap.put(this.sourcesFile, "SO");
        }
        if (this.titlesFile != null) {
            fileLabelMap.put(this.titlesFile, "TI");
        }

        // build GODDAGS and write them to files
        List<File> referenceGoddagFiles = new ArrayList<File>();
        GoddagBuilder goddagBuilder = new GoddagBuilder("RO", wordSplitRegex);
        for (File referenceFile : this.referenceDirectory.listFiles()) {

            String[] references = FileUtils.readFile(referenceFile).split("\n");

            List<Goddag> referenceGoddags = new ArrayList<Goddag>();
            for (String reference : references) {

                reference = ReferenceNormalizer.splitAfterPunctuation(reference);

                Goddag goddag = goddagBuilder.build(reference);

                referenceGoddags.add(goddag);
            }
            String outputFileName = this.outputDirectory.getAbsolutePath() + "/"
                    + FilenameUtils.removeExtension(referenceFile.getName()) + ".json";
            File outputFile = new File(outputFileName);
            org.apache.commons.io.FileUtils.writeStringToFile(outputFile,
                    this.gson.toJson(referenceGoddags, ArrayList.class));
            referenceGoddagFiles.add(outputFile);
        }

        if (this.authorsFile != null) {
            NameTagger nameTagger = new NameTagger("AU", "FN", "LN", wordNormalizer);

            System.out.println("read name file");
            String authorsString = FileUtils.readFile(this.authorsFile);
            System.out.println("\tread given names");
            List<String> givenNames = CsvUtils.readColumn(0, authorsString, columnSeparator);
            System.out.println("\tread surnames");
            List<String> surnames = CsvUtils.readColumn(1, authorsString, columnSeparator);
            List<Name> authorNames = new ArrayList<Name>();
            for (int i = 0; i < givenNames.size(); i++) {
                authorNames.add(new Name(givenNames.get(i), surnames.get(i)));
            }
            System.out.println("read names into tagger");
            nameTagger.readAuthors(authorNames, true);

            System.out.println("run tagger");
            for (File referenceGoddagFile : referenceGoddagFiles) {
                this.runTagger(nameTagger, referenceGoddagFile);
            }
        }

        for (Entry<File, String> fileLabelEntry : fileLabelMap.entrySet()) {
            StringTagger stringTagger = new StringTagger(fileLabelEntry.getValue(), wordSplitRegex, wordNormalizer);

            stringTagger.readStrings(fileLabelEntry.getKey(), columnSeparator);

            for (File referenceGoddagFile : referenceGoddagFiles) {
                this.runTagger(stringTagger, referenceGoddagFile);
            }
        }

    }

    private void runTagger(Tagger tagger, File goddagFile) throws IOException {
        Goddag[] referenceGoddags = GoddagUtils.deserializeGoddags(FileUtils.readFile(goddagFile));

        for (Goddag referenceGoddag : referenceGoddags) {
            tagger.tag(referenceGoddag);
        }

        if (goddagFile.exists()) {
            goddagFile.delete();
        }

        org.apache.commons.io.FileUtils.writeStringToFile(goddagFile,
                this.gson.toJson(referenceGoddags, Goddag[].class));
    }
}
