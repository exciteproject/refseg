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
import de.mkrnr.rse.train.CRFByLabelLikelihoodTrainer;
import de.mkrnr.rse.train.Trainer;
import de.mkrnr.rse.util.Deserializer;
import de.mkrnr.rse.util.FileHelper;
import de.mkrnr.rse.util.FileMerger;
import de.mkrnr.rse.util.Serializer;

public class CrossValidator {

    public static void main(String[] args) {
        File inputDirectory = new File("/home/martin/tmp/papers/20-test-extr");
        // File inputDirectory = new
        // File("/home/martin/tmp/papers/2-test-extr");
        File crossValidationParentDirectory = new File("/home/martin/tmp/eval/");
        int numberOfFolds = 12;

        List<String> featuresNames = new ArrayList<String>();
        featuresNames.add("CAPITALIZED");
        featuresNames.add("ONELETTER");
        featuresNames.add("ENDSWITHPERIOD");
        featuresNames.add("ENDSWITHCOMMA");

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

        SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

        SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(featuresNames);
        // FileHelper.resetDirectory(evalDictionary);

        // create/load folds
        CRFByLabelLikelihoodTrainer crfTrainer = new CRFByLabelLikelihoodTrainer(serialPipes);

        Evaluator crfEvaluator = new Evaluator(serialPipes);

        CrossValidator crossValidator = new CrossValidator(crfTrainer, crfEvaluator);

        File crossValidationDirectory = new File(crossValidationParentDirectory + File.separator
                + inputDirectory.getName() + "-" + numberOfFolds + "-fold");
        File foldsDirectory = new File(crossValidationDirectory + File.separator + "folds");

        List<Fold> folds = crossValidator.splitIntoFolds(inputDirectory, numberOfFolds);
        crossValidator.saveFolds(folds, foldsDirectory);
        // List<Fold> folds = crossValidator.loadFolds(foldsDirectory);

        // evaluate folds

        Evaluations evaluations = crossValidator.validate(folds);

        evaluations.writeEvaluations(new File(crossValidationDirectory + File.separator + "evaluations.json"));
        evaluations.writeAggregatedResults(new File(crossValidationDirectory + File.separator + "aggregated.json"));
    }

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

    private Evaluations validate(List<Fold> folds) {
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

    // TODO aggregate results

}
