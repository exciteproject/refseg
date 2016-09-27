package de.exciteproject.refseg.distsup;

import java.util.List;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

public class GoddagNameStructure {

    public enum NodeType {
        ROOT("[Root]"), AUTHOR("[Author]"), NAME("[Name]"), FIRST_NAME("[FirstName]"), LAST_NAME("[LastName]");

        private final String val;

        private NodeType(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return this.val;
        }
    }

    public static void main(String[] args) {
        GoddagNameStructure goddagAuthorStructure = new GoddagNameStructure();
        Node leafNode1 = goddagAuthorStructure.addAsLeafNode("Max");
        Node leafNode2 = goddagAuthorStructure.addAsLeafNode("A.");
        Node leafNode3 = goddagAuthorStructure.addAsLeafNode("Friedrich,");
        Node leafNode4 = goddagAuthorStructure.addAsLeafNode("Müller");
        goddagAuthorStructure.addAsLeafNode("(2004)");

        Node firstNameNode1 = goddagAuthorStructure.createMatchNode(NodeType.FIRST_NAME.toString());
        Node firstNameNode2 = goddagAuthorStructure.createMatchNode(NodeType.FIRST_NAME.toString());
        Node firstNameNode3 = goddagAuthorStructure.createMatchNode(NodeType.FIRST_NAME.toString());
        Node lastNameNode1 = goddagAuthorStructure.createMatchNode(NodeType.LAST_NAME.toString());
        Node lastNameNode2 = goddagAuthorStructure.createMatchNode(NodeType.LAST_NAME.toString());
        goddagAuthorStructure.tagNodeAs(leafNode1, firstNameNode1);
        goddagAuthorStructure.tagNodeAs(leafNode2, firstNameNode1);
        goddagAuthorStructure.tagNodeAs(leafNode2, firstNameNode2);
        goddagAuthorStructure.tagNodeAs(leafNode3, firstNameNode2);
        goddagAuthorStructure.tagNodeAs(leafNode3, firstNameNode3);
        goddagAuthorStructure.tagNodeAs(leafNode3, lastNameNode1);
        goddagAuthorStructure.tagNodeAs(leafNode4, lastNameNode2);

        System.out.println(goddagAuthorStructure.toString());

        // List<Node> rootChildren = goddagAuthorStructure.getRootChildren();
        // for (Node rootChild : rootChildren) {
        // System.out.println(rootChild);
        // }
    }

    private Goddag goddag;

    public GoddagNameStructure() {
        this.goddag = new Goddag();
        Node rootNode = this.goddag.createNonterminalNode(NodeType.ROOT.toString());
        this.goddag.setRootNode(rootNode);
    }

    public GoddagNameStructure(Goddag goddag) {
        this.goddag = goddag;
    }

    public Node addAsLeafNode(Node parentNode, String string) {
        Node leafNode = this.goddag.createLeafNode(string);
        parentNode.addChild(leafNode);
        return leafNode;
    }

    public Node addAsLeafNode(String string) {
        Node leafNode = this.goddag.createLeafNode(string);
        this.goddag.getRootNode().addChild(leafNode);
        return leafNode;
    }

    public Node createMatchNode(String string) {
        return this.goddag.createNonterminalNode(string);
    }

    public Goddag getGoddag() {
        return this.goddag;
    }

    public List<Node> getLeafNodes() {
        return this.goddag.getLeafNodes();
    }

    public void tagNodeAs(Node node, Node nodeToInsert) {
        this.goddag.insertNodeBetween(this.goddag.getRootNode(), node, nodeToInsert);
    }

    public Node tagNodesAs(Node[] leafNodes, String nonterminalNodeType) {
        Node nonterminalNode = this.goddag.createNonterminalNode(nonterminalNodeType);
        for (Node leafNode : leafNodes) {
            this.goddag.insertNodeBetween(this.goddag.getRootNode(), leafNode, nonterminalNode);
        }
        return nonterminalNode;
    }

    @Override
    public String toString() {
        return this.goddag.toString();
    }
}
