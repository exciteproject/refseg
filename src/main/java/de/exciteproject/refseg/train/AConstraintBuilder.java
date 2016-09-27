package de.exciteproject.refseg.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import de.exciteproject.refseg.distsup.GoddagNameStructure;
import de.exciteproject.refseg.util.JsonHelper;
import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

//TODO refactor the whole class...
public class AConstraintBuilder {

    public class NameDistribution {

        public double authorCount;
        public double otherCount;

        public double getSum() {
            return this.authorCount + this.otherCount;
        }
    }

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
        AConstraintBuilder nameConstraintBuilder = new AConstraintBuilder();

        String jsonFileToUsePath = args[0];
        String goddagDictionaryPath = args[1];
        String constraintsOutputFilePath = args[2];
        boolean addEndTag = Boolean.parseBoolean(args[3]);
        double nonAuthorRatio = Double.parseDouble(args[4]);
        int nonAuthorCount = Integer.parseInt(args[5]);
        boolean fixPercentages = Boolean.parseBoolean(args[6]);
        double otherPercentage = Double.parseDouble(args[7]);

        File goddagDirectory = new File(goddagDictionaryPath);

        @SuppressWarnings("unchecked")
        List<File> filesToUse = (List<File>) JsonHelper.readFromFile(new TypeToken<List<File>>() {
        }.getType(), new File(jsonFileToUsePath));
        Set<String> fileIdsToUse = new HashSet<String>();
        for (File inputFile : filesToUse) {
            fileIdsToUse.add(FilenameUtils.removeExtension(inputFile.getName()));
        }

        nameConstraintBuilder.extractAuthorStatistics(goddagDirectory, fileIdsToUse, addEndTag, nonAuthorRatio,
                nonAuthorCount, fixPercentages, otherPercentage);
        // nameConstraintBuilder.printContraints();
        nameConstraintBuilder.writeDistributions(new File(constraintsOutputFilePath), "A", "O", addEndTag);
    }

    private Gson gson;

    private Map<String, NameDistribution> nameDistributions;

    public AConstraintBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
        this.gson = gsonBuilder.create();

        this.nameDistributions = new HashMap<String, NameDistribution>();

    }

    /**
     *
     * @param goddagDirectory
     * @param fileIdsToUse
     * @param addEndTag
     * @param nonAuthorRatio
     *            if 2.0: amount of non-author tags two times the amount of
     *            author tags
     * @param addAuthorPercentages
     *            adds author percentages for non-author tags based on the
     *            distribution of tagged authors
     * @param otherPercentage
     *            estimated percentage of other tags in final distribution; used
     *            for probability distribution for non-author labels
     *
     * @throws JsonSyntaxException
     * @throws JsonIOException
     * @throws FileNotFoundException
     */
    public void extractAuthorStatistics(File goddagDirectory, Set<String> fileIdsToUse, boolean addEndTag,
            double nonAuthorRatio, int nonAuthorCount, boolean addAuthorPercentages, double otherPercentage)
            throws JsonSyntaxException, JsonIOException, FileNotFoundException {

        int totalAuthorCount = 0;
        int totalNonAuthorNodes = 0;

        // go over goddag files the first time to get author tags
        for (File goddagFile : goddagDirectory.listFiles()) {
            String goddagFileId = FilenameUtils.removeExtension(goddagFile.getName());
            if (fileIdsToUse.contains(goddagFileId)) {
                Goddag goddag = this.gson.fromJson(new FileReader(goddagFile), Goddag.class);
                GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);

                // iterate over goddag tree to count first names and last names
                for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
                    if (rootChildNode.getLabel().equals(GoddagNameStructure.NodeType.AUTHOR.toString())) {
                        List<Node> authorNodeChildren = rootChildNode.getChildren();
                        for (int childeNodeIndex = 0; childeNodeIndex < authorNodeChildren.size(); childeNodeIndex++) {
                            Node authorChild = authorNodeChildren.get(childeNodeIndex);
                            String nameWord = authorChild.getFirstChild().getLabel();
                            if (!this.nameDistributions.containsKey(nameWord)) {
                                this.nameDistributions.put(nameWord, new NameDistribution());
                            }
                            this.nameDistributions.get(nameWord).authorCount += 1;
                            totalAuthorCount++;
                        }
                    } else {
                        totalNonAuthorNodes += 1;
                    }
                }
            }
        }
        double authorPercentage = 0.0;
        double oPercentage = 1.0;

        if (addAuthorPercentages) {
            authorPercentage = 1 - oPercentage;
            oPercentage = otherPercentage;
        }
        Double nonAuthorPercentage = null;

        if (nonAuthorRatio > 0.0) {
            nonAuthorPercentage = (nonAuthorRatio * totalAuthorCount) / totalNonAuthorNodes;
            if (nonAuthorCount > 0) {
                throw new IllegalArgumentException(
                        "not both nonAuthorRation and nonAuthorCount can be set greater than 0");
            }
        } else {
            if (nonAuthorCount > 0) {
                nonAuthorPercentage = ((double) nonAuthorCount) / totalNonAuthorNodes;
            } else {
                throw new IllegalArgumentException(
                        "either nonAuthorRation or nonAtuhorCount have to be greater than 0");
            }
        }
        if (nonAuthorPercentage > 1.0) {
            throw new IllegalStateException("nonAuthorPercentage is over 1.0: " + nonAuthorPercentage);
        }
        // go over goddag files a second time to get non-author tags based on
        // statistics
        for (File goddagFile : goddagDirectory.listFiles()) {
            String goddagFileId = FilenameUtils.removeExtension(goddagFile.getName());
            if (fileIdsToUse.contains(goddagFileId)) {
                Goddag goddag = this.gson.fromJson(new FileReader(goddagFile), Goddag.class);
                GoddagNameStructure goddagNameStructure = new GoddagNameStructure(goddag);

                for (Node rootChildNode : goddagNameStructure.getGoddag().getRootNode().getChildren()) {
                    if (!rootChildNode.getLabel().equals(GoddagNameStructure.NodeType.AUTHOR.toString())) {

                        if (Math.random() < nonAuthorPercentage) {
                            Node childNode = rootChildNode;
                            while (childNode.hasChildren()) {
                                childNode = childNode.getFirstChild();
                            }
                            String word = childNode.getLabel();
                            if (word.isEmpty()) {
                                continue;
                            }
                            if (!this.nameDistributions.containsKey(word)) {
                                this.nameDistributions.put(word, new NameDistribution());
                            }

                            this.nameDistributions.get(word).authorCount += authorPercentage;
                            this.nameDistributions.get(word).otherCount += oPercentage;
                        }
                    }
                }
            }
        }

    }

    public void printContraints() {
        for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
            System.out.println(nameEntry.getKey());
            System.out.println("\tA: " + nameEntry.getValue().authorCount);
            System.out.println("\tO: " + nameEntry.getValue().otherCount);
        }
    }

    /**
     * stores extracted author statistics in a file according to
     * cc.mallet.fst.semi_supervised.FSTConstraintUtil
     *
     * the file contains lines similar to: Friedrich A:0.8 O:0.2
     *
     * @param outputFile
     * @param authorLabel
     *            label for author distribution, is not allowed to contain
     *            colons
     * @param otherLabel
     *            label for other, is not allowed to contain colons
     * @throws IOException
     */
    public int writeDistributions(File outputFile, String authorLabel, String otherLabel, boolean addEndTags)
            throws IOException {
        int nameCount = 0;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
            String name = nameEntry.getKey();
            if (name.contains(" ")) {
                bufferedWriter.close();
                throw new IllegalStateException("name contains space: " + name);
            }

            nameCount += 1;

            double authorPercentage = nameEntry.getValue().authorCount / nameEntry.getValue().getSum();
            double otherPercentage = nameEntry.getValue().otherCount / nameEntry.getValue().getSum();
            String line = name;
            line += " ";
            line += authorLabel + ":" + authorPercentage;
            line += " ";
            line += otherLabel + ":" + otherPercentage;

            line += System.lineSeparator();
            bufferedWriter.write(line);
        }
        bufferedWriter.close();
        return nameCount;
    }

}
