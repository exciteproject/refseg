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
public class FnLnConstraintBuilder {

    public class NameDistribution {

        public double bFirstNameCount;
        public double bLastNameCount;
        public double eFirstNameCount;
        public double eLastNameCount;
        public double iFirstNameCount;
        public double iLastNameCount;
        public double otherCount;
        public double iOtherCount;

        public double getSum() {
            return this.bFirstNameCount + this.bLastNameCount + this.eFirstNameCount + this.eLastNameCount
                    + this.iFirstNameCount + this.iLastNameCount + this.otherCount;
        }
    }

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {
        FnLnConstraintBuilder nameConstraintBuilder = new FnLnConstraintBuilder();

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
        nameConstraintBuilder.writeDistributions(new File(constraintsOutputFilePath), "B-FN", "B-LN", "E-FN", "E-LN",
                "I-FN", "I-LN", "I-O", "O", addEndTag);
    }

    private Gson gson;

    private Map<String, NameDistribution> nameDistributions;

    public FnLnConstraintBuilder() {
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

        int totalBFnCount = 0;
        int totalBLnCount = 0;
        int totalEFnCount = 0;
        int totalELnCount = 0;
        int totalIFnCount = 0;
        int totalILnCount = 0;
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
                            if ((authorChild.getLabel().equals(GoddagNameStructure.NodeType.FIRST_NAME.toString()))) {
                                if (childeNodeIndex == 0) {
                                    this.nameDistributions.get(nameWord).bFirstNameCount += 1;
                                    totalBFnCount++;
                                } else {
                                    if (addEndTag && (childeNodeIndex == (authorNodeChildren.size() - 1))) {
                                        this.nameDistributions.get(nameWord).eFirstNameCount += 1;
                                        totalEFnCount++;
                                    } else {
                                        this.nameDistributions.get(nameWord).iFirstNameCount += 1;
                                        totalIFnCount++;
                                    }
                                }
                            } else {
                                if (authorChild.getLabel().equals(GoddagNameStructure.NodeType.LAST_NAME.toString())) {
                                    if (childeNodeIndex == 0) {
                                        this.nameDistributions.get(nameWord).bLastNameCount += 1;
                                        totalBLnCount++;
                                    } else {
                                        if (addEndTag && (childeNodeIndex == (authorNodeChildren.size() - 1))) {
                                            this.nameDistributions.get(nameWord).eLastNameCount += 1;
                                            totalELnCount++;
                                        } else {
                                            this.nameDistributions.get(nameWord).iLastNameCount += 1;
                                            totalILnCount++;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        totalNonAuthorNodes += 1;
                    }
                }
            }
        }
        double bFnPercentage = 0.0;
        double bLnPercentage = 0.0;
        double eFnPercentage = 0.0;
        double eLnPercentage = 0.0;
        double iFnPercentage = 0.0;
        double iLnPercentage = 0.0;
        double iOPercentage = 0.0;
        double oPercentage = 1.0;

        int totalTaggedCount = totalBFnCount + totalBLnCount + totalEFnCount + totalELnCount + totalIFnCount
                + totalILnCount;

        double authorPercentage = 1.0 - otherPercentage;
        if (addAuthorPercentages) {
            bFnPercentage = ((double) totalBFnCount / totalTaggedCount) * authorPercentage;
            bLnPercentage = ((double) totalBLnCount / totalTaggedCount) * authorPercentage;
            eFnPercentage = ((double) totalEFnCount / totalTaggedCount) * authorPercentage;
            eLnPercentage = ((double) totalELnCount / totalTaggedCount) * authorPercentage;
            iFnPercentage = ((double) totalIFnCount / totalTaggedCount) * authorPercentage;
            iLnPercentage = ((double) totalILnCount / totalTaggedCount) * authorPercentage;
            iOPercentage = 0.0;
            oPercentage = otherPercentage;
            double totalOtherProbability = bFnPercentage + bLnPercentage + eFnPercentage + eLnPercentage + iFnPercentage
                    + iLnPercentage + oPercentage;
            if ((Math.abs(totalOtherProbability) - 1) > 0.0001) {
                throw new IllegalStateException("probabilities don't sum to 1: " + totalOtherProbability);
            }
        }
        Double nonAuthorPercentage = null;

        if (nonAuthorRatio > 0.0) {
            nonAuthorPercentage = (nonAuthorRatio * totalTaggedCount) / totalNonAuthorNodes;
            if (nonAuthorCount > 0) {
                throw new IllegalArgumentException(
                        "not both nonAuthorRation and nonAuthorCount can be set greater than 0");
            }
        } else {
            if (nonAuthorCount > 0) {
                nonAuthorPercentage = ((double) nonAuthorCount) / totalNonAuthorNodes;
            } else {
                // both are zero
                nonAuthorPercentage = 0.0;
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

                            this.nameDistributions.get(word).bFirstNameCount += bFnPercentage;
                            this.nameDistributions.get(word).bLastNameCount += bLnPercentage;
                            if (addEndTag) {
                                this.nameDistributions.get(word).eFirstNameCount += eFnPercentage;
                                this.nameDistributions.get(word).eLastNameCount += eLnPercentage;
                            }
                            this.nameDistributions.get(word).iFirstNameCount += iFnPercentage;
                            this.nameDistributions.get(word).iLastNameCount += iLnPercentage;
                            this.nameDistributions.get(word).iOtherCount += iOPercentage;
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
            System.out.println("\tB-FN: " + nameEntry.getValue().bFirstNameCount);
            System.out.println("\tB-LN: " + nameEntry.getValue().bLastNameCount);
            System.out.println("\tE-FN: " + nameEntry.getValue().eFirstNameCount);
            System.out.println("\tE-LN: " + nameEntry.getValue().eLastNameCount);
            System.out.println("\tI-FN: " + nameEntry.getValue().iFirstNameCount);
            System.out.println("\tI-LN: " + nameEntry.getValue().iLastNameCount);
            System.out.println("\tI-O: " + nameEntry.getValue().iOtherCount);
            System.out.println("\tO: " + nameEntry.getValue().otherCount);
        }
    }

    /**
     * stores extracted author statistics in a file according to
     * cc.mallet.fst.semi_supervised.FSTConstraintUtil
     *
     * the file contains lines similar to: Friedrich B-FN:0.2 B-LN:0.1 ...
     *
     * @param outputFile
     * @param bFirstNameLabel
     *            label for beginning first name distribution, is not allowed to
     *            contain colons
     * @param bLastNameLabel
     *            label for beginning last name distribution, is not allowed to
     *            contain colons
     * @param eFirstNameLabel
     *            label for ending first name distribution, is not allowed to
     *            contain colons
     * @param eLastNameLabel
     *            label for ending last name distribution, is not allowed to
     *            contain colons
     * @param iFirstNameLabel
     *            label for intermediate first name distribution, is not allowed
     *            to contain colons
     * @param iLastNameLabel
     *            label for intermediate last name distribution, is not allowed
     *            to contain colons
     * @param iOtherLabel
     *            label for intermediate other distribution, is not allowed to
     *            contain colons
     * @param otherLabel
     *            label for other distribution, is not allowed to contain colons
     *
     * @throws IOException
     */
    public int writeDistributions(File outputFile, String bFirstNameLabel, String bLastNameLabel,
            String eFirstNameLabel, String eLastNameLabel, String iFirstNameLabel, String iLastNameLabel,
            String iOtherLabel, String otherLabel, boolean addEndTags) throws IOException {
        int nameCount = 0;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (Entry<String, NameDistribution> nameEntry : this.nameDistributions.entrySet()) {
            String name = nameEntry.getKey();
            if (name.contains(" ")) {
                bufferedWriter.close();
                throw new IllegalStateException("name contains space: " + name);
            }

            nameCount += 1;

            double bFnPercentage = nameEntry.getValue().bFirstNameCount / nameEntry.getValue().getSum();
            double bLnPercentage = nameEntry.getValue().bLastNameCount / nameEntry.getValue().getSum();
            double iFnPercentage = nameEntry.getValue().iFirstNameCount / nameEntry.getValue().getSum();
            double iLnPercentage = nameEntry.getValue().iLastNameCount / nameEntry.getValue().getSum();
            double eFnPercentage = nameEntry.getValue().eFirstNameCount / nameEntry.getValue().getSum();
            double eLnPercentage = nameEntry.getValue().eLastNameCount / nameEntry.getValue().getSum();
            double iOtherPercentage = nameEntry.getValue().iOtherCount / nameEntry.getValue().getSum();
            double otherPercentage = nameEntry.getValue().otherCount / nameEntry.getValue().getSum();
            String line = name;
            line += " ";
            line += bFirstNameLabel + ":" + bFnPercentage;
            line += " ";
            line += bLastNameLabel + ":" + bLnPercentage;
            line += " ";
            if (addEndTags) {
                line += eFirstNameLabel + ":" + eFnPercentage;
                line += " ";
                line += eLastNameLabel + ":" + eLnPercentage;
                line += " ";
            }
            line += iFirstNameLabel + ":" + iFnPercentage;
            line += " ";
            line += iLastNameLabel + ":" + iLnPercentage;
            line += " ";
            line += iOtherLabel + ":" + iOtherPercentage;
            line += " ";
            line += otherLabel + ":" + otherPercentage;

            line += System.lineSeparator();
            bufferedWriter.write(line);
        }
        bufferedWriter.close();
        return nameCount;
    }

}
