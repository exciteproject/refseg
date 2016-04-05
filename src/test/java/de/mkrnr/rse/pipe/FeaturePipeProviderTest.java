package de.mkrnr.rse.pipe;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInput;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;
import junit.framework.Assert;

public class FeaturePipeProviderTest {

    private static FeaturePipeProvider featurePipeProvider;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        featurePipeProvider = new FeaturePipeProvider(null, null);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private ArrayList<String> testStrings;

    private ArrayList<Boolean> expectedResults;

    private void compareResults(String result, String featureName) {
        String[] resultSplit = result.split("\\n");

        for (int i = 0; i < resultSplit.length; i++) {
            Assert.assertEquals(this.expectedResults.get(i).booleanValue(), resultSplit[i].contains(featureName));
        }
    }

    private String runPipe(Pipe pipe) {
        // generate inputString for the pipe from tests
        String inputString = "";
        for (String testString : this.testStrings) {
            inputString += testString + "\n";
        }
        inputString = inputString.replaceFirst("\n$", "");

        // set pipes
        ArrayList<Pipe> pipes = new ArrayList<Pipe>();

        pipes.add(new Input2CharSequence("UTF-8"));
        pipes.add(new SimpleTaggerSentence2TokenSequence());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        pipes.add(pipe);
        pipes.add(new TokenSequence2FeatureVectorSequence());
        pipes.add(new PrintInput(printStream));

        InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
        instanceList.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(IOUtils.toInputStream(inputString))),
                        Pattern.compile("^\\s*$"), true));
        String output = "";
        try {
            output = byteArrayOutputStream.toString("UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // remove line with class name
        output = output.replaceFirst(".*\\n", "");
        return output;

    }

    private void setTest(String testString, boolean expectedResult) {
        this.testStrings.add(testString);
        this.expectedResults.add(expectedResult);
    }

    @Before
    public void setUp() throws Exception {
        this.testStrings = new ArrayList<String>();
        this.expectedResults = new ArrayList<Boolean>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCapitalized() {
        String featureName = "CAPITALIZED";

        this.setTest("Test", true);
        this.setTest("T", true);
        this.setTest("Östlich", true);
        this.setTest("Ö", true);
        this.setTest("T!st", true);

        this.setTest("test", false);
        this.setTest("t", false);
        this.setTest("östlich", false);
        this.setTest("ö", false);
        this.setTest("1est", false);
        this.setTest(".test", false);
        this.setTest(".", false);
        this.setTest("!.", false);
        this.setTest("1234", false);
        this.setTest("_", false);

        String result = this.runPipe(featurePipeProvider.getPipe(featureName));

        this.compareResults(result, featureName);
    }

    @Test
    public void testOneLetter() {
        String featureName = "ONELETTER";

        this.setTest("T", true);
        this.setTest("t", true);
        this.setTest("Ö", true);
        this.setTest("ö", true);
        this.setTest(".n", true);
        this.setTest("1n", true);
        this.setTest("1n1", true);

        this.setTest("1", false);
        this.setTest("$", false);
        this.setTest("Nö", false);
        this.setTest("nö", false);
        this.setTest("1nö", false);
        this.setTest("1-nö", false);

        String result = this.runPipe(featurePipeProvider.getPipe(featureName));

        this.compareResults(result, featureName);
    }

}
