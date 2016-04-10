package de.mkrnr.rse.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.mkrnr.rse.train.Trainer;
import de.mkrnr.rse.util.FileHelper;
import de.mkrnr.rse.util.FileMerger;
import de.mkrnr.rse.util.JsonHelper;

public class CrossValidator {

    private Trainer trainer;
    private Evaluator evaluator;

    public CrossValidator(Trainer trainer, Evaluator evaluator) {
        this.trainer = trainer;
        this.evaluator = evaluator;

    }

    public List<Fold> loadFolds(File foldsDirectory) {
        List<Fold> folds = new ArrayList<Fold>();

        File[] seralizedFoldFiles = foldsDirectory.listFiles();
        Arrays.sort(seralizedFoldFiles);

        for (File serializedFoldFile : seralizedFoldFiles) {
            folds.add((Fold) JsonHelper.readFromFile(Fold.class, serializedFoldFile));
        }
        return folds;
    }

    public void saveFolds(List<Fold> folds, File outputDirectory) {
        // delete existing directory and create new one
        FileHelper.resetDirectory(outputDirectory);

        for (Fold fold : folds) {
            JsonHelper.writeToFile(fold,
                    new File(outputDirectory.getAbsolutePath() + File.separator + fold.getName() + ".json"));
        }
    }

    /**
     * Splits the files in fileDirectory into folds based on the number of
     * papers. Thereby, papers are not split into different folds.
     *
     * @param inputDirectory
     *
     * @param numberOfFolds
     *            Number of folds
     */
    public List<Fold> splitIntoFolds(File inputDirectory, int numberOfFolds) {

        File[] allFiles = inputDirectory.listFiles();
        ArrayList<File> remainingFiles = new ArrayList<File>(Arrays.asList(inputDirectory.listFiles()));

        if (numberOfFolds > allFiles.length) {
            throw new IllegalStateException("More folds than files in directory");
        }

        // shuffle files
        Collections.shuffle(remainingFiles);

        ArrayList<Fold> folds = new ArrayList<Fold>();

        for (int foldIndex = 0; foldIndex < numberOfFolds; foldIndex++) {

            // TODO add fold information in result printout
            Fold fold = new Fold(inputDirectory.getName() + "-" + numberOfFolds + "-fold-" + foldIndex);

            int remainingFolds = numberOfFolds - foldIndex;

            int filesinFold = (remainingFiles.size()) / remainingFolds;

            HashSet<File> testingFiles = new HashSet<File>();

            // get testing files
            for (int i = 0; i < filesinFold; i++) {
                testingFiles.add(remainingFiles.remove(0));
            }

            // add files to fold
            for (File file : allFiles) {
                if (testingFiles.contains(file)) {
                    fold.addTestingFile(file);
                } else {
                    fold.addTrainingFile(file);
                }
            }

            folds.add(fold);
        }

        return folds;
    }

    public void validate(Fold fold) {
    }

    public Evaluations validate(List<Fold> folds) {
        Evaluations evaluations = new Evaluations();
        for (Fold fold : folds) {
            System.out.println("Run evaluation on:");
            fold.printFoldInformation();

            File trainingFile = FileMerger.mergeFiles(fold.getTrainingFiles(),
                    this.getTempFile(fold.getName() + "-train"));
            File testingFile = FileMerger.mergeFiles(fold.getTestingFiles(),
                    this.getTempFile(fold.getName() + "-test"));

            this.trainer.train(trainingFile, testingFile, true);

            Evaluation evaluation = this.evaluator.evaluate(this.trainer.getTransducerTrainer(), testingFile);
            evaluation.setName(fold.getName());

            evaluations.addEvaluation(evaluation);

        }
        return evaluations;
    }

    private File getTempFile(String filePrefix) {
        String tempFileName = filePrefix + "-" + System.nanoTime();
        File tempFile = null;
        try {
            tempFile = File.createTempFile(tempFileName, ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    // TODO aggregate results

}
