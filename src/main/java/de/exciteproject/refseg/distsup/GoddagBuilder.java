package de.exciteproject.refseg.distsup;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

/**
 * Class for building Goddags by segmenting an input string into individual
 * words using wordSplitRegex which the form the leaf nodes
 */
public class GoddagBuilder {
    private String wordSplitRegex;
    private String rootNodeLabel;

    public GoddagBuilder(String rootNodeLabel, String wordSplitRegex) {
        this.wordSplitRegex = wordSplitRegex;
        this.rootNodeLabel = rootNodeLabel;
    }

    public Goddag build(String inputString) {
        Goddag goddag = new Goddag();

        Node rootNode = goddag.createNonterminalNode(this.rootNodeLabel);
        goddag.setRootNode(rootNode);

        String[] inputStringSplit = inputString.split(this.wordSplitRegex);
        for (String inputStringPart : inputStringSplit) {
            Node leafNode = goddag.createLeafNode(inputStringPart);

            goddag.getRootNode().addChild(leafNode);

        }
        return goddag;

    }
}
