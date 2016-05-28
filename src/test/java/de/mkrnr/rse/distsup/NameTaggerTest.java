package de.mkrnr.rse.distsup;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mkrnr.goddag.Goddag;

public class NameTaggerTest {

    private static NameTagger nameTagger;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	NameTaggerTest.nameTagger = new NameTagger(true);
	Map<String, Integer> firstNames = new HashMap<String, Integer>();
	// firstNames.add("Max");
	firstNames.put("Max", 1);
	firstNames.put("C.", 1);
	firstNames.put("Tim", 1);
	firstNames.put("Norbert", 1);
	firstNames.put("Günter", 1);
	firstNames.put("Fritz", 1);
	nameTagger.createNameMap(firstNames, true, GoddagNameStructure.NodeType.FIRST_NAME.toString());

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
	String inputString = "123 Max C. Tim NotAName";
	Goddag test = nameTagger.tagString(inputString);
	System.out.println(test.toString());

    }

}
