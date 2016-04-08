package de.mkrnr.rse.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import cc.mallet.pipe.SerialPipes;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.train.CRFTrainer;
import de.mkrnr.rse.util.Deserializer;
import de.mkrnr.rse.util.FileHelper;
import de.mkrnr.rse.util.FileMerger;
import de.mkrnr.rse.util.Serializer;

public class CrossValidator {

    public static void main(String[] args) {
        File inputDirectory = new File("/home/martin/tmp/papers/2-test-extr");
        File crossValidationDirectory = new File("/home/martin/tmp/eval/2-test-extr/");
        List<String> featuresNames = new ArrayList<String>();
        featuresNames.add("CAPITALIZED");
        featuresNames.add("ONELETTER");
        featuresNames.add("ENDSWITHPERIOD");
        featuresNames.add("ENDSWITHCOMMA");

        // FileHelper.resetDirectory(evalDictionary);

        // create/load folds
        CrossValidator crossValidator = new CrossValidator();
        int numberOfFolds = 2;
        File foldsDirectory = new File(crossValidationDirectory + File.separator + "folds");

        List<Fold> folds = crossValidator.splitIntoFolds(inputDirectory, numberOfFolds);
        crossValidator.saveFolds(folds, foldsDirectory);
        // List<Fold> folds = crossValidator.loadFolds(foldsDirectory);

        // evaluate folds

        Evaluations evaluations = new Evaluations();
        for (Fold fold : folds) {
            System.out.println("Run evaluation on:");
            fold.printFoldInformation();
            FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

            SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

            SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(featuresNames);

            CRFTrainer crfTrainer = new CRFTrainer(serialPipes);

            File trainingFile = FileMerger.mergeFiles(fold.getTrainingFiles(),
                    crossValidator.getTempFile(fold.getName() + "-train"));
            File testingFile = FileMerger.mergeFiles(fold.getTestingFiles(),
                    crossValidator.getTempFile(fold.getName() + "-test"));
            crfTrainer.trainByLabelLikelihood(trainingFile, testingFile, true);

            System.out.println("Evaluation:");
            TransducerTrainerEvaluator crfEvaluator = new TransducerTrainerEvaluator(serialPipes,
                    crfTrainer.getTrainer());

            Evaluation evaluation = crfEvaluator.evaluate(trainingFile, testingFile);
            evaluation.setFold(fold);
            evaluations.addEvaluation(evaluation);

        }
        evaluations.aggregate();

    }

    public List<Fold> loadFolds(File foldsDirectory) {
        List<Fold> folds = new ArrayList<Fold>();

        File[] seralizedFoldFiles = foldsDirectory.listFiles();
        Arrays.sort(seralizedFoldFiles);

        for (File serializedFoldFile : seralizedFoldFiles) {
            folds.add((Fold) Deserializer.deserialize(serializedFoldFile));
        }
        return folds;
    }

    public void saveFolds(List<Fold> folds, File outputDirectory) {
        // delete existing directory and create new one
        FileHelper.resetDirectory(outputDirectory);

        for (Fold fold : folds) {
            Serializer.serialize(fold,
                    new File(outputDirectory.getAbsolutePath() + File.separator + fold.getName() + ".ser"));
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

        File testingFile = FileMerger.mergeFiles(fold.getTestingFiles(), this.getTempFile("testing"));
        File trainingFile = FileMerger.mergeFiles(fold.getTrainingFiles(), this.getTempFile("training"));

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

        SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

        // TODO handle CRF configuration in additional class
        List<String> featuresNames = new ArrayList<String>();
        featuresNames.add("CAPITALIZED");
        featuresNames.add("ONELETTER");
        featuresNames.add("ENDSWITHPERIOD");
        featuresNames.add("ENDSWITHCOMMA");

        SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(featuresNames);

        CRFTrainer crfTrainer = new CRFTrainer(serialPipes);

        crfTrainer.trainByLabelLikelihood(trainingFile, testingFile, true);

        System.out.println("Evaluation:");
        TransducerTrainerEvaluator crfEvaluator = new TransducerTrainerEvaluator(serialPipes, crfTrainer.getTrainer());
        crfEvaluator.evaluate(trainingFile, testingFile);
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
