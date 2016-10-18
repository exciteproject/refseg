package de.exciteproject.refseg.distsup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;
import de.mkrnr.goddag.visual.GoddagVisualizer;

public class StringTagger implements Tagger {

    public static void main(String[] args) throws IOException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonSerializer());
        gsonBuilder.registerTypeAdapter(Node.class, Node.getJsonSerializer());
        Gson gson = gsonBuilder.create();

        String wordSplitRegex = "\\s";

        List<String> words = new ArrayList<String>();

        words.add("tes");
        words.add("test");
        words.add("ein test");
        words.add("ein test. test");
        long startTime;
        long endTime;
        startTime = System.nanoTime();

        String inputString = "das ist ein test test";

        WordNormalizer wordNormalizer = new StartEndWordNormalizer();

        StringTagger labelTagger = new StringTagger("[T]", wordSplitRegex, wordNormalizer);
        labelTagger.readStrings(words);
        GoddagBuilder goddagBuilder = new GoddagBuilder("[R]", wordSplitRegex);
        Goddag goddag = goddagBuilder.build(inputString);
        labelTagger.tag(goddag);

        endTime = System.nanoTime();
        System.out.println("Tagging took " + ((endTime - startTime) / 1000000) + " milliseconds");

        GoddagVisualizer goddagVisualizer = new GoddagVisualizer();
        goddagVisualizer.visualize(goddag);
        FileUtils.writeStringToFile(new File("/home/mkoerner/tmp/tagged.json"), gson.toJson(goddag, Goddag.class));

        //// Getting the runtime reference from system
        // Runtime runtime = Runtime.getRuntime();

        // int mb = 1024 * 1024;
        // System.out.println("##### Heap utilization statistics [MB] #####");

        //// Print used memory
        // System.out.println("Used Memory:" + ((runtime.totalMemory() -
        //// runtime.freeMemory()) / mb));

        //// Print free memory
        // System.out.println("Free Memory:" + (runtime.freeMemory() / mb));

        //// Print total available memory
        // System.out.println("Total Memory:" + (runtime.totalMemory() / mb));

        //// Print Maximum available memory
        // System.out.println("Max Memory:" + (runtime.maxMemory() / mb));
    }

    private Trie<String, Integer> trie;

    private WordNormalizer wordNormalizer;
    private String wordSplitRegex;
    private String label;

    public StringTagger(String label, String wordSplitRegex, WordNormalizer wordNormalizer) {
        this.label = label;
        this.wordSplitRegex = wordSplitRegex;
        this.wordNormalizer = wordNormalizer;
    }

    public void readStrings(List<String> strings) {

        this.trie = new PatriciaTrie<Integer>();

        for (String string : strings) {

            String[] words = string.split(this.wordSplitRegex);
            for (int i = 0; i < words.length; i++) {
                words[i] = this.wordNormalizer.normalizeWord(words[i]);
            }
            String normalizedSequence = "";
            for (String normalizedWord : words) {
                normalizedSequence += normalizedWord + " ";

            }
            normalizedSequence = normalizedSequence.trim();

            this.trie.put(normalizedSequence, 0);
        }

    }

    @Override
    public void tag(Goddag goddag) {
        for (int startIndex = 0; startIndex < goddag.getLeafNodes().size(); startIndex++) {
            String searchString = "";
            int currentIndex = startIndex;

            SortedMap<String, Integer> trieSearchResult = null;
            do {
                searchString += this.wordNormalizer.normalizeWord(goddag.getLeafNodes().get(currentIndex).getLabel());
                trieSearchResult = this.trie.prefixMap(searchString);
                if (trieSearchResult.containsKey(searchString)) {
                    // tag from startIndex until currentIndex
                    Node tagNode = goddag.createNonterminalNode(this.label);
                    for (int tagIndex = startIndex; tagIndex <= currentIndex; tagIndex++) {
                        goddag.insertNodeBetween(goddag.getRootNode(), goddag.getLeafNodes().get(tagIndex), tagNode);
                    }
                }
                searchString += " ";
                currentIndex++;
            } while ((trieSearchResult.size() > 0) && (currentIndex < goddag.getLeafNodes().size()));
        }
    }
}
