package de.mkrnr.rse.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class CrossValidatorTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private File testInputDirectory;
    private File testFoldsDirectory;
    private TransducerCrossValidator crossValidator;

    @Before
    public void setUp() throws Exception {
        // generate testDirectory
        this.testInputDirectory = new File("src/test/resources/test-input-directory");
        this.testFoldsDirectory = new File("src/test/resources/test-folds-directory");

        this.testInputDirectory.mkdir();

        this.crossValidator = new TransducerCrossValidator(null, null);
        // crossValidator.validate(fileDirectory, folds);
    }

    @After
    public void tearDown() throws Exception {
        // delete testDirectory
        FileUtils.deleteDirectory(this.testInputDirectory);
        FileUtils.deleteDirectory(this.testFoldsDirectory);
    }

    @Test
    public void testSplitEven() {
        int numberOfFiles = 10;
        int numberOfFolds = 5;

        this.generateFiles(numberOfFiles);

        ArrayList<Fold> folds = new ArrayList<Fold>(
                this.crossValidator.splitIntoFolds(this.testInputDirectory, numberOfFolds));

        this.evaluateFolds(folds, numberOfFolds, numberOfFiles);
    }

    @Test
    public void testSplitOdd1() {
        int numberOfFiles = 10;
        int numberOfFolds = 3;

        this.generateFiles(numberOfFiles);

        ArrayList<Fold> folds = new ArrayList<Fold>(
                this.crossValidator.splitIntoFolds(this.testInputDirectory, numberOfFolds));

        this.evaluateFolds(folds, numberOfFolds, numberOfFiles);
    }

    @Test
    public void testSplitOdd2() {
        int numberOfFiles = 11;
        int numberOfFolds = 3;

        this.generateFiles(numberOfFiles);

        ArrayList<Fold> folds = new ArrayList<Fold>(
                this.crossValidator.splitIntoFolds(this.testInputDirectory, numberOfFolds));

        this.evaluateFolds(folds, numberOfFolds, numberOfFiles);
    }

    @Test(expected = IllegalStateException.class)
    public void testSplitTooManyFolds() {
        int numberOfFiles = 2;
        int numberOfFolds = 3;

        this.generateFiles(numberOfFiles);

        new ArrayList<Fold>(this.crossValidator.splitIntoFolds(this.testInputDirectory, numberOfFolds));
    }

    private void evaluateFolds(ArrayList<Fold> folds, int numberOfFolds, int numberOfFiles) {
        Assert.assertEquals(numberOfFolds, folds.size());

        int minTestingFilesInFold = numberOfFiles / numberOfFolds;
        int maxTestingFilesInFold = (int) Math.ceil(((double) numberOfFiles) / numberOfFolds);

        int numberOfTotalTestingFiles = 0;
        for (Fold fold : folds) {
            int testingFilesInFold = fold.getTestingFiles().size();
            int trainingFilesInFold = fold.getTrainingFiles().size();

            Assert.assertTrue(testingFilesInFold >= minTestingFilesInFold);
            Assert.assertTrue(testingFilesInFold <= maxTestingFilesInFold);

            Assert.assertTrue(trainingFilesInFold >= (numberOfFiles - maxTestingFilesInFold));
            Assert.assertTrue(trainingFilesInFold <= (numberOfFiles - minTestingFilesInFold));

            numberOfTotalTestingFiles += fold.getTestingFiles().size();
        }
        Assert.assertEquals(numberOfFiles, numberOfTotalTestingFiles);
    }

    private void generateFiles(int numberOfFiles) {
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(this.testInputDirectory.getAbsolutePath() + File.separator + i + ".txt");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
