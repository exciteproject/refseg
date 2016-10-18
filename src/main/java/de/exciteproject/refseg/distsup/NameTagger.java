package de.exciteproject.refseg.distsup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

public class NameTagger implements Tagger {

    public static void main(String[] args) throws IOException {

        long startTime;
        long endTime;

        String wordSplitRegex = "\\s";
        GoddagBuilder goddagBuilder = new GoddagBuilder("[R]", wordSplitRegex);

        // String inputString = null;
        // try {
        // inputString = FileHelper.readFile(inputFile);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        String inputString = "test1 Max F Müller M test2";

        Goddag goddag = goddagBuilder.build(inputString);

        WordNormalizer wordNormalizer = new StartEndWordNormalizer();
        NameTagger authorTagger = new NameTagger("[A]", "[FN]", "[LN]", wordNormalizer);

        List<Name> names = new ArrayList<Name>();
        names.add(new Name("Max", "Müller"));
        names.add(new Name("Max Friedrich", "Müller"));
        names.add(new Name("Friedrich", "Müller"));

        authorTagger.readAuthors(names, true);

        startTime = System.nanoTime();
        authorTagger.tag(goddag);

        // authorTagger.readAuthors(new File(args[0]), true);

        endTime = System.nanoTime();

        // GoddagVisualizer goddagVisualizer = new GoddagVisualizer();
        // goddagVisualizer.visualize(goddag);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonSerializer());
        gsonBuilder.registerTypeAdapter(Node.class, Node.getJsonSerializer());
        Gson gson = gsonBuilder.create();
        FileUtils.writeStringToFile(new File("/home/mkoerner/tmp/names-tagged.json"),
                gson.toJson(goddag, Goddag.class));

        System.out.println(goddag);
        System.out.println("Building tries took " + ((endTime - startTime) / 1000000) + " milliseconds");

        System.out.println("Tagging took " + ((endTime - startTime) / 1000000) + " milliseconds");

        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        int mb = 1024 * 1024;
        System.out.println("##### Heap utilization statistics [MB] #####");

        // Print used memory
        System.out.println("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / mb));

        // Print free memory
        System.out.println("Free Memory:" + (runtime.freeMemory() / mb));

        // Print total available memory
        System.out.println("Total Memory:" + (runtime.totalMemory() / mb));

        // Print Maximum available memory
        System.out.println("Max Memory:" + (runtime.maxMemory() / mb));
    }

    // order:Map<LastNames,Set<FirstNames>>
    private Map<String, Set<String>> names;
    private Set<String> firstNames;
    private Set<String> lastNames;

    private String nameLabel;
    private String firstNameLabel;
    private String lastNameLabel;
    private WordNormalizer wordNormalizer;

    public NameTagger(String nameLabel, String firstNameLabel, String lastNameLabel, WordNormalizer wordNormalizer) {
        this.firstNameLabel = firstNameLabel;
        this.lastNameLabel = lastNameLabel;
        this.nameLabel = nameLabel;
        this.wordNormalizer = wordNormalizer;

        this.names = new HashMap<String, Set<String>>();
        this.firstNames = new HashSet<String>();
        this.lastNames = new HashSet<String>();
    }

    public void readAuthors(List<Name> names, boolean createFirstNameVariations) throws IOException {

        // normalize words

        for (Name name : names) {
            Set<String> firstNameVariations;
            if (createFirstNameVariations) {
                firstNameVariations = NameVariationBuilder.getFirstNameVariations(name.getFirstName());
            } else {
                firstNameVariations = new HashSet<String>();
                firstNameVariations.add(name.getFirstName());
            }

            String lastName = name.getLastName();

            // add to firstNames
            for (String firstNameVariation : firstNameVariations) {
                this.firstNames.add(firstNameVariation);
            }
            // add to lastNames
            this.lastNames.add(lastName);

            Set<String> firstNamesOfLastName;
            if (!this.names.containsKey(lastName)) {
                firstNamesOfLastName = new HashSet<String>();
                this.names.put(lastName, firstNamesOfLastName);
            } else {
                firstNamesOfLastName = this.names.get(lastName);
            }

            for (String firstNameVariation : firstNameVariations) {
                firstNamesOfLastName.add(firstNameVariation);
            }
        }

    }

    @Override
    public void tag(Goddag goddag) {

        // tag leaf nodes as first and last name
        for (Node leafNode : goddag.getLeafNodes()) {
            String normalizedWord = this.wordNormalizer.normalizeWord(leafNode.getLabel());
            if (this.firstNames.contains(normalizedWord)) {
                Node firstNameNode = goddag.createNonterminalNode(this.firstNameLabel);
                goddag.insertNodeBetween(goddag.getRootNode(), leafNode, firstNameNode);
            }

            if (this.lastNames.contains(normalizedWord)) {
                Node lastNameNode = goddag.createNonterminalNode(this.lastNameLabel);
                goddag.insertNodeBetween(goddag.getRootNode(), leafNode, lastNameNode);
            }
        }

        // search for author names
        for (int leafNodeIndex = 0; leafNodeIndex < goddag.getLeafNodes().size(); leafNodeIndex++) {

            List<Node> leafNodes = goddag.getLeafNodes();

            // search for fn...fn ln...ln
            List<Node> firstFirstNames = this.searchNodesWithParentLabel(leafNodes, leafNodeIndex, this.firstNameLabel);
            if (firstFirstNames.size() > 0) {
                int secondLastNameStartIndex = leafNodeIndex + firstFirstNames.size();

                List<Node> secondLastNames = this.searchNodesWithParentLabel(leafNodes, secondLastNameStartIndex,
                        this.lastNameLabel);

                for (int secondIndex = 0; secondIndex < secondLastNames.size(); secondIndex++) {
                    List<Node> currentLastNames = new ArrayList<Node>();
                    for (int i = 0; i <= secondIndex; i++) {
                        currentLastNames.add(leafNodes.get(secondLastNameStartIndex));
                    }
                    if (this.searchAuthor(firstFirstNames, currentLastNames)) {
                        Node authorNode = goddag.createNonterminalNode(this.nameLabel);
                        this.addToAuthorNode(firstFirstNames, this.firstNameLabel, authorNode, goddag);
                        this.addToAuthorNode(currentLastNames, this.lastNameLabel, authorNode, goddag);
                    }
                }
            }

            // search for ln...ln fn...fn
            List<Node> firstLastNames = this.searchNodesWithParentLabel(leafNodes, leafNodeIndex, this.lastNameLabel);
            if (firstLastNames.size() > 0) {
                int secondFirstNameStartIndex = leafNodeIndex + firstLastNames.size();

                List<Node> secondFirstNames = this.searchNodesWithParentLabel(leafNodes, secondFirstNameStartIndex,
                        this.firstNameLabel);

                for (int secondIndex = 0; secondIndex < secondFirstNames.size(); secondIndex++) {
                    List<Node> currentFirstNames = new ArrayList<Node>();
                    for (int i = 0; i <= secondIndex; i++) {
                        currentFirstNames.add(leafNodes.get(secondFirstNameStartIndex));
                    }
                    if (this.searchAuthor(currentFirstNames, firstLastNames)) {
                        Node authorNode = goddag.createNonterminalNode(this.nameLabel);
                        this.addToAuthorNode(firstLastNames, this.lastNameLabel, authorNode, goddag);
                        this.addToAuthorNode(currentFirstNames, this.firstNameLabel, authorNode, goddag);
                    }
                }
            }
        }
    }

    private void addToAuthorNode(List<Node> nodesToAdd, String parentNodeLabel, Node authorNode, Goddag goddag) {
        for (Node node : nodesToAdd) {
            for (Node parentNode : node.getParents()) {
                if (parentNode.getLabel().equals(parentNodeLabel)) {
                    goddag.insertNodeBetween(goddag.getRootNode(), parentNode, authorNode);
                    break;
                }
            }
        }
    }

    private String concatNormalizedNodeLabels(List<Node> nodes) {
        String result = "";
        for (Node node : nodes) {
            result += this.wordNormalizer.normalizeWord(node.getLabel());
            result += " ";
        }
        result = result.replaceFirst("\\s$", "");
        return result;
    }

    private boolean searchAuthor(List<Node> firstNameNodes, List<Node> lastNameNodes) {
        String lastName = this.concatNormalizedNodeLabels(lastNameNodes);
        if (this.names.containsKey(lastName)) {
            String firstName = this.concatNormalizedNodeLabels(firstNameNodes);
            if (this.names.get(lastName).contains(firstName)) {
                return true;
            }
        }
        return false;
    }

    private List<Node> searchNodesWithParentLabel(List<Node> leafNodes, int startLeafNodeIndex, String parentLabel) {
        List<Node> foundNodes = new ArrayList<Node>();

        for (int leafNodeIndex = startLeafNodeIndex; leafNodeIndex < leafNodes.size(); leafNodeIndex++) {
            Node leafNode = leafNodes.get(leafNodeIndex);

            // search for parentLabel
            boolean hasParentLabel = false;
            for (Node parentNode : leafNode.getParents()) {
                if (parentNode.getLabel().equals(parentLabel)) {
                    hasParentLabel = true;
                }
            }

            if (hasParentLabel) {
                foundNodes.add(leafNode);
            } else {
                break;
            }
        }

        return foundNodes;
    }

}
