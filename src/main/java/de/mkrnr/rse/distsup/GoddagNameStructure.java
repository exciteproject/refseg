package de.mkrnr.rse.distsup;

import java.util.Iterator;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.LeafNode;
import de.mkrnr.goddag.NonterminalNode;

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
	LeafNode leafNode1 = goddagAuthorStructure.addAsLeafNode("Max");
	LeafNode leafNode2 = goddagAuthorStructure.addAsLeafNode("A.");
	LeafNode leafNode3 = goddagAuthorStructure.addAsLeafNode("Friedrich,");
	LeafNode leafNode4 = goddagAuthorStructure.addAsLeafNode("Müller");
	goddagAuthorStructure.addAsLeafNode("(2004)");

	goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode1, leafNode2 }, NodeType.FIRST_NAME);
	goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode3 }, NodeType.LAST_NAME);
	goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode3 }, NodeType.FIRST_NAME);
	goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode4 }, NodeType.LAST_NAME);

	System.out.println(goddagAuthorStructure.toString());
    }

    private Goddag goddag;

    public GoddagNameStructure() {
	this.goddag = new Goddag();
	NonterminalNode<NodeType> rootNode = this.goddag.createNonterminalNode(NodeType.ROOT);
	this.goddag.setRootNode(rootNode);
    }

    public LeafNode addAsLeafNode(String string) {
	LeafNode leafNode = this.goddag.createLeafNode(string);
	this.goddag.getRootNode().addChild(leafNode);
	return leafNode;
    }

    public Iterator<LeafNode> getLeafNodeIterator() {
	return this.goddag.getLeafNodeIterator();
    }

    public NonterminalNode<NodeType> tagNodesAs(LeafNode[] leafNodes, NodeType nonterminalNodeType) {
	NonterminalNode<NodeType> nonterminalNode = this.goddag.createNonterminalNode(nonterminalNodeType);
	for (LeafNode leafNode : leafNodes) {
	    this.goddag.insertNodeBetween(this.goddag.getRootNode(), leafNode, nonterminalNode);
	}
	return nonterminalNode;
    }

    @Override
    public String toString() {
	return this.goddag.toString();
    }
}
