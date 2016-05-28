package de.mkrnr.rse.distsup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mkrnr.goddag.Node;
import de.mkrnr.goddag.visual.GoddagVisualizer;
import de.mkrnr.rse.distsup.GoddagNameStructure.NodeType;

public class GoddagNameStructureTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private GoddagNameStructure goddagAuthorStructure;

    @Before
    public void setUp() throws Exception {
	this.goddagAuthorStructure = new GoddagNameStructure();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
	Node leafNode1 = this.goddagAuthorStructure.addAsLeafNode("Max");
	Node leafNode2 = this.goddagAuthorStructure.addAsLeafNode("C.");
	Node leafNode3 = this.goddagAuthorStructure.addAsLeafNode("Tim,");

	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode2 }, NodeType.FIRST_NAME.toString());
	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode2, leafNode3 }, NodeType.FIRST_NAME.toString());
	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode1 }, NodeType.FIRST_NAME.toString());
	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode3 }, NodeType.FIRST_NAME.toString());

	System.out.println(this.goddagAuthorStructure.toString());

    }

    @Test
    public void writeTest() {
	this.goddagAuthorStructure.addAsLeafNode("Test");
	Node leafNode1 = this.goddagAuthorStructure.addAsLeafNode("Max");
	Node leafNode2 = this.goddagAuthorStructure.addAsLeafNode("C.");
	Node leafNode3 = this.goddagAuthorStructure.addAsLeafNode("Tim,");
	this.goddagAuthorStructure.addAsLeafNode("Test");

	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode2 }, NodeType.FIRST_NAME.toString());
	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode2, leafNode3 }, NodeType.FIRST_NAME.toString());
	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode1 }, NodeType.FIRST_NAME.toString());
	this.goddagAuthorStructure.tagNodesAs(new Node[] { leafNode3 }, NodeType.FIRST_NAME.toString());

	GoddagVisualizer goddagVisualizer = new GoddagVisualizer();
	goddagVisualizer.visualize(this.goddagAuthorStructure.getGoddag());
	try {
	    Thread.sleep(100000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// JsonHelper.writeToFile(this.goddagAuthorStructure.getGoddag(), new
	// File("/home/martin/test-output.json"));
	// System.out.println(this.goddagAuthorStructure.toString());
    }
}
