package de.mkrnr.rse.eval;

import java.io.File;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import cc.mallet.pipe.SerialPipes;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.train.CRFBuilder;
import de.mkrnr.rse.train.CRFTrainerByLabelLikelihoodBuilder;
import de.mkrnr.rse.train.CRFTrainerFactory;
import de.mkrnr.rse.train.TransducerTrainerBuilder;
import de.mkrnr.rse.train.TransducerTrainerFactory;
import de.mkrnr.rse.util.Configuration;
import de.mkrnr.rse.util.ConfigurationConverter;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();

        JCommander jCommander;
        try {
            jCommander = new JCommander(main, args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        if (main.help) {
            jCommander.usage();
        } else {
            main.run();
        }
    }

    @Parameter(names = { "-create",
            "--create-folds" }, description = "New folds are created if set, otherwise existing folds are loaded")
    private boolean create = false;

    @Parameter(names = { "-eval",
            "--evaluation-dir" }, description = "directory in which the evaluation results are stored and which contains the folds directory", required = true, converter = FileConverter.class)
    private File evaluationDirectory;

    @Parameter(names = { "-feat",
            "--features" }, description = "comma separated list of features", variableArity = true, required = true)
    private List<String> features;

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters")
    private boolean help;

    @Parameter(names = { "-input",
            "--input-dir" }, description = "input directory containing preprocessed text files for MALLET evaluation", required = true, converter = FileConverter.class)
    private File inputDirectory;

    @Parameter(names = { "-folds",
            "--number-of-folds" }, description = "number of folds for cross validation", required = true)
    private Integer numberOfFolds;

    @Parameter(names = { "-crf",
            "--crf-configurations" }, description = "list of key=value for configuring the CRF building", variableArity = true, required = true, converter = ConfigurationConverter.class)
    private List<Configuration> crfConfigurations;

    @Parameter(names = { "-trainer",
            "--trainer-configurations" }, description = "list of key=value for configuring the trainer building", variableArity = true, required = true, converter = ConfigurationConverter.class)
    private List<Configuration> transducerTrainerConfigurations;

    @Parameter(names = { "-other",
            "--other-label" }, description = "label that is used to specitfy other instances in the training set")
    private String otherLabel = "other";

    private void run() {

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

        SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

        SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(this.features);

        CRFBuilder crfBuilder = new CRFBuilder(this.crfConfigurations, serialPipes, null);

        TransducerTrainerBuilder transducerTrainerBuilder = null;

        // TODO add option for choosing the trainer
        if (true) {
            CRFTrainerByLabelLikelihoodBuilder crfTrainerByLabelLikelihoodBuilder = new CRFTrainerByLabelLikelihoodBuilder(
                    this.transducerTrainerConfigurations);

            transducerTrainerBuilder = crfTrainerByLabelLikelihoodBuilder;
        }

        TransducerTrainerFactory transducerTrainerFactory = new CRFTrainerFactory(crfBuilder, transducerTrainerBuilder);

        StructuredTransducerEvaluatorFactory structuredTransducerEvaluatorFactory = new StructuredPerClassAccuracyEvaluatorFactory();

        TransducerCrossValidator crossValidator = new TransducerCrossValidator(transducerTrainerFactory,
                structuredTransducerEvaluatorFactory, this.otherLabel);

        File foldsDirectory = new File(this.evaluationDirectory + File.separator + "folds");

        // create/load folds
        List<Fold> folds = null;
        if (this.create) {
            folds = crossValidator.splitIntoFolds(this.inputDirectory, this.numberOfFolds);
            crossValidator.saveFolds(folds, foldsDirectory);
        } else {
            folds = crossValidator.loadFolds(foldsDirectory);
        }

        // evaluate folds
        Evaluations evaluations = crossValidator.validate(folds, serialPipes);

        evaluations.writeEvaluations(new File(this.evaluationDirectory + File.separator + "evaluations" + ".json"));
        evaluations
                .writeAggregatedResults(new File(this.evaluationDirectory + File.separator + "aggregated" + ".json"));
    }
}
