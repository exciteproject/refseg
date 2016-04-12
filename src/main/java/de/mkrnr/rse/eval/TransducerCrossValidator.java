package de.mkrnr.rse.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.train.TransducerTrainerFactory;
import de.mkrnr.rse.util.FileMerger;
import de.mkrnr.rse.util.InstanceListBuilder;
import de.mkrnr.rse.util.JsonHelper;

public class TransducerCrossValidator {

    private TransducerTrainerFactory transducerTrainerFactory;
    private StructuredTransducerEvaluatorFactory structuredTransducerEvaluatorFactory;
    private String otherLabel;

    public TransducerCrossValidator(TransducerTrainerFactory transducerTrainerFactory,
            StructuredTransducerEvaluatorFactory structuredTransducerEvaluatorFactory, String otherLabel) {
        this.transducerTrainerFactory = transducerTrainerFactory;
        this.structuredTransducerEvaluatorFactory = structuredTransducerEvaluatorFactory;
        this.otherLabel = otherLabel;

    }

    public Folds loadFolds(File foldsFile) {
        Folds folds = new Folds();

        folds = (Folds) JsonHelper.readFromFile(Folds.class, foldsFile);

        return folds;
    }

    public void saveFolds(Folds folds, File outputFile) {
        JsonHelper.writeToFile(folds, outputFile);
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
    public Folds splitIntoFolds(File inputDirectory, int numberOfFolds) {

        File[] allFiles = inputDirectory.listFiles();
        ArrayList<File> remainingFiles = new ArrayList<File>(Arrays.asList(inputDirectory.listFiles()));

        if (numberOfFolds > allFiles.length) {
            throw new IllegalStateException("More folds than files in directory");
        }

        // shuffle files
        Collections.shuffle(remainingFiles);

        Folds folds = new Folds();

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

    public TransducerEvaluations validate(Folds folds, Pipe inputPipe, boolean evaluateDuringTraining) {
        TransducerEvaluations transducerEvaluations = new TransducerEvaluations();
        for (Fold fold : folds.asList()) {
            System.out.println("Run evaluation on:");
            fold.printFoldInformation();

            File trainingFile = FileMerger.mergeFiles(fold.getTrainingFiles(),
                    this.getTempFile(fold.getName() + "-train"));
            File testingFile = FileMerger.mergeFiles(fold.getTestingFiles(),
                    this.getTempFile(fold.getName() + "-test"));
            InstanceList trainingInstances = InstanceListBuilder.build(trainingFile, inputPipe);
            InstanceList testingInstances = InstanceListBuilder.build(testingFile, inputPipe);

            TransducerTrainer transducerTrainer = this.transducerTrainerFactory.getTransducerTrainer(trainingInstances,
                    testingInstances, evaluateDuringTraining);
            transducerTrainer.train(trainingInstances);

            StructuredTransducerEvaluator structuredTransducerEvaluator = this.structuredTransducerEvaluatorFactory
                    .getStructuredTransducerEvaluator(testingInstances, "testing", this.otherLabel);
            structuredTransducerEvaluator.evaluate(transducerTrainer);
            Evaluation evaluation = structuredTransducerEvaluator.getEvaluation();
            evaluation.setName(fold.getName());

            transducerEvaluations.addEvaluation(evaluation);

        }
        return transducerEvaluations;
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
}
