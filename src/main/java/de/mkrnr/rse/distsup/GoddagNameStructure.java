package de.mkrnr.rse.distsup;

import java.util.List;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

public class GoddagNameStructure {

    public enum NodeType {
	ROOT("Root"), NAME("Name"), FIRST_NAME("FirstName"), LAST_NAME("LastName");

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

	goddagAuthorStructure.tagNodesAs(new Node[] { leafNode1, leafNode2 }, NodeType.FIRST_NAME.toString());
	goddagAuthorStructure.tagNodesAs(new Node[] { leafNode2, leafNode3 }, NodeType.FIRST_NAME.toString());
	goddagAuthorStructure.tagNodesAs(new Node[] { leafNode3 }, NodeType.FIRST_NAME.toString());
	goddagAuthorStructure.tagNodesAs(new Node[] { leafNode3 }, NodeType.LAST_NAME.toString());
	goddagAuthorStructure.tagNodesAs(new Node[] { leafNode4 }, NodeType.LAST_NAME.toString());

	System.out.println(goddagAuthorStructure.toString());

	List<Node> rootChildren = goddagAuthorStructure.getRootChildren();
	for (Node rootChild : rootChildren) {
	    System.out.println(rootChild);
	}
    }

    private Goddag goddag;

    public GoddagNameStructure() {
	this.goddag = new Goddag();
	Node rootNode = this.goddag.createNonterminalNode(NodeType.ROOT.toString());
	this.goddag.setRootNode(rootNode);
    }

    public Node addAsLeafNode(String string) {
	Node leafNode = this.goddag.createLeafNode(string);
	this.goddag.getRootNode().addChild(leafNode);
	return leafNode;
    }

    public Goddag getGoddag() {
	return this.goddag;
    }

    public List<Node> getLeafNodes() {
	return this.goddag.getLeafNodes();
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

    private List<Node> getRootChildren() {

	return this.goddag.getRootNode().getChildren();
    }
}
