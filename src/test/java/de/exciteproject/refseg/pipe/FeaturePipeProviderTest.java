package de.exciteproject.refseg.pipe;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
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
        featurePipeProvider = new FeaturePipeProvider();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private ArrayList<String> testStrings;

    private ArrayList<Boolean> expectedResults;

    private void checkResults(String result, String featureName) {
        String[] resultSplit = result.split("\\n");

        for (int i = 0; i < resultSplit.length; i++) {
            Assert.assertEquals(this.expectedResults.get(i).booleanValue(), resultSplit[i].contains(featureName));
        }
    }

    private String createTestString() {
        String inputString = "";
        for (String testString : this.testStrings) {
            inputString += testString + "\n";
        }
        inputString = inputString.replaceFirst("\n$", "");
        return inputString;
    }

    private String runPipe(String featureName) {
        // create testString
        String testString = this.createTestString();

        // set pipes
        Pipe featurePipe = featurePipeProvider.getPipe(featureName);

        ArrayList<Pipe> pipes = new ArrayList<Pipe>();

        pipes.add(new Input2CharSequence("UTF-8"));
        pipes.add(new SimpleTaggerSentence2TokenSequence());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        pipes.add(featurePipe);
        pipes.add(new TokenSequence2FeatureVectorSequence());
        pipes.add(new PrintInput(printStream));

        InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
        instanceList.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(IOUtils.toInputStream(testString))),
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
    public void testAllCaps() {
        String featureName = "ALLCAPS";

        this.setTest("TEST", true);
        this.setTest("T", true);
        this.setTest("ÖSTLICH", true);
        this.setTest("Ö", true);
        this.setTest("T!ST", true);
        this.setTest("T.S.", true);
        this.setTest("T.S", true);

        this.setTest("test", false);
        this.setTest("Test", false);
        this.setTest("t", false);
        this.setTest("Östlich", false);
        this.setTest("ö", false);
        this.setTest("1eST", false);
        this.setTest(".tEST", false);
        this.setTest(".", false);
        this.setTest("!.", false);
        this.setTest("1234", false);
        this.setTest("_", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testAllLowerCase() {
        String featureName = "ALLLOWERCASE";

        this.setTest("test", true);
        this.setTest("test.", true);
        this.setTest("t", true);
        this.setTest("t3st", true);

        this.setTest("TEST", false);
        this.setTest("T", false);
        this.setTest("ÖSTLICH", false);
        this.setTest("Ö", false);
        this.setTest("T!ST", false);
        this.setTest("T.S.", false);
        this.setTest("T.S", false);

        this.setTest(".", false);
        this.setTest("!.", false);
        this.setTest("1234", false);
        this.setTest("_", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testBraces() {
        String featureName = "BRACES";

        this.setTest("(Test)", true);
        this.setTest("(test)", true);
        this.setTest("(42)", true);
        this.setTest("(42).", true);
        this.setTest("(42),", true);
        this.setTest("()", true);
        this.setTest("(())", true);
        this.setTest("(()", true);
        this.setTest("())", true);
        this.setTest("test(test)test", true);
        this.setTest("(test)test", true);
        this.setTest("4(test)2", true);

        this.setTest("Test", false);
        this.setTest("]test[", false);
        this.setTest(")test(", false);
        this.setTest(")test(", false);
        this.setTest("(test", false);
        this.setTest("test)", false);
        this.setTest("test(", false);
        this.setTest("(test", false);
        this.setTest(")test", false);
        this.setTest(")(", false);
        this.setTest("((", false);
        this.setTest("))", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testBrackets() {
        String featureName = "BRACKETS";

        this.setTest("[Test]", true);
        this.setTest("[test]", true);
        this.setTest("[42]", true);
        this.setTest("[42].", true);
        this.setTest("[42],", true);
        this.setTest("[]", true);
        this.setTest("[[]]", true);
        this.setTest("[[]", true);
        this.setTest("[]]", true);
        this.setTest("test[test]test", true);
        this.setTest("[test]test", true);
        this.setTest("4[test]2", true);

        this.setTest("Test", false);
        this.setTest("(test)", false);
        this.setTest("]test[", false);
        this.setTest(")test(", false);
        this.setTest("[test", false);
        this.setTest("test]", false);
        this.setTest("test[", false);
        this.setTest("]test", false);
        this.setTest("]test", false);
        this.setTest("][", false);
        this.setTest("[[", false);
        this.setTest("]]", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
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

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testEndsWithComma() {
        String featureName = "ENDSWITHCOMMA";

        this.setTest("Test,", true);
        this.setTest("test,", true);
        this.setTest("Ö,", true);
        this.setTest("ö,", true);
        this.setTest(",", true);
        this.setTest("1,", true);
        this.setTest(",,", true);

        this.setTest(",Test", false);
        this.setTest(",test", false);
        this.setTest(",t", false);
        this.setTest(",.", false);
        this.setTest(",)", false);
        this.setTest(",]", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testEndsWithPeriod() {
        String featureName = "ENDSWITHPERIOD";

        this.setTest("Test.", true);
        this.setTest("test.", true);
        this.setTest("Ö.", true);
        this.setTest("ö.", true);
        this.setTest(".", true);
        this.setTest("1.", true);
        this.setTest("..", true);

        this.setTest(".Test", false);
        this.setTest(".test", false);
        this.setTest(".t", false);
        this.setTest(".,", false);
        this.setTest(".)", false);
        this.setTest(".]", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testMonth() {
        String featureName = "MONTH";

        this.setTest("June", true);
        this.setTest("june", true);
        this.setTest("JUNE", true);
        this.setTest("Jun", true);
        this.setTest("jUNE", true);
        this.setTest("January.", true);
        this.setTest(".May", true);
        this.setTest("(Dec)", true);
        this.setTest(")Dec(", true);
        this.setTest("June(today)", true);
        this.setTest("today(June)", true);
        this.setTest("(toda)June(today)", true);
        this.setTest("13-June", true);
        this.setTest("13.December", true);
        this.setTest("13December", true);
        this.setTest("13December13", true);

        this.setTest("test", false);
        this.setTest("Junetest", false);
        this.setTest("testjune", false);
        this.setTest("juneapril", false);
        this.setTest("Ju-ne", false);
        this.setTest("jüne", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testNumber() {
        String featureName = "NUMBER";

        this.setTest("42", true);
        this.setTest("4", true);
        this.setTest("t3st", true);
        this.setTest("42test", true);
        this.setTest("test42", true);
        this.setTest("(42)", true);
        this.setTest("42.", true);

        this.setTest("Test", false);
        this.setTest("42.42", false);
        this.setTest("42test42", false);
        this.setTest(".", false);
        this.setTest("fourtytwo", false);
        this.setTest("!@#$%^&*()-=", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testNumbers() {
        String featureName = "NUMBERS";

        this.setTest("42.42", true);
        this.setTest("42test42", true);
        this.setTest("(42test42)", true);
        this.setTest("(42-42)", true);
        this.setTest("(42--42)", true);
        this.setTest("(42-42-42)", true);

        this.setTest("Test", false);
        this.setTest("42", false);
        this.setTest("4", false);
        this.setTest("t3st", false);
        this.setTest("42test", false);
        this.setTest("test42", false);
        this.setTest("(42)", false);
        this.setTest("42.", false);
        this.setTest("fourty.two", false);
        this.setTest("!@#$%^&*()-=", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
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

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testPeriod() {
        String featureName = "PERIOD";

        this.setTest("Test.", true);
        this.setTest("test.", true);
        this.setTest(".test", true);
        this.setTest("te.st", true);
        this.setTest(".t", true);
        this.setTest(".", true);
        this.setTest("!.", true);

        this.setTest(".test.", false);
        this.setTest("te.st.", false);
        this.setTest("te..st", false);
        this.setTest("..", false);
        this.setTest(".!.", false);
        this.setTest(".1.", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testPeriods() {
        String featureName = "PERIODS";

        this.setTest("Te.st.", true);
        this.setTest(".test.", true);
        this.setTest("..test", true);
        this.setTest("te..st", true);
        this.setTest(".t.", true);
        this.setTest("..", true);
        this.setTest(".1.", true);
        this.setTest(".!.", true);

        this.setTest("test.", false);
        this.setTest("test.", false);
        this.setTest("te.st", false);
        this.setTest(".", false);
        this.setTest("!.", false);
        this.setTest(".,", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }

    @Test
    public void testYear() {
        String featureName = "YEAR";

        int year = Calendar.getInstance().get(Calendar.YEAR);

        this.setTest("2000", true);
        this.setTest(String.valueOf(year), true);
        this.setTest("1700", true);
        this.setTest("1600", true);
        this.setTest("(1995)", true);
        this.setTest("[1995]", true);
        this.setTest("year1995", true);
        this.setTest(")1995(", true);

        this.setTest("1599", false);
        this.setTest(String.valueOf(year + 1), false);
        this.setTest("3000", false);
        this.setTest("01.2001", false);
        this.setTest("1.1.2001", false);
        this.setTest("1/1/2001", false);
        this.setTest("2001/1/1", false);
        this.setTest("11700", false);
        this.setTest("17001", false);

        String results = this.runPipe(featureName);

        this.checkResults(results, featureName);
    }
}
