package de.mkrnr.rse.distsup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mkrnr.goddag.LeafNode;
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
	LeafNode leafNode1 = this.goddagAuthorStructure.addAsLeafNode("Max");
	LeafNode leafNode2 = this.goddagAuthorStructure.addAsLeafNode("C.");
	LeafNode leafNode3 = this.goddagAuthorStructure.addAsLeafNode("Tim,");

	this.goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode2 }, NodeType.FIRST_NAME);
	this.goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode2, leafNode3 }, NodeType.FIRST_NAME);
	this.goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode1 }, NodeType.FIRST_NAME);
	this.goddagAuthorStructure.tagNodesAs(new LeafNode[] { leafNode3 }, NodeType.FIRST_NAME);

	System.out.println(this.goddagAuthorStructure.toString());
    }

}
