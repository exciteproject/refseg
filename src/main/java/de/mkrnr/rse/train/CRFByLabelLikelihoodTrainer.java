package de.mkrnr.rse.train;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.eval.Evaluator;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.util.InstanceListBuilder;

public class CRFByLabelLikelihoodTrainer implements Trainer {

    public static void main(String[] args) {

        File trainingFile = new File(args[0]);
        File testingFile = new File(args[1]);

        FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

        SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

        List<String> featuresNames = new ArrayList<String>();
        featuresNames.add("CAPITALIZED");
        featuresNames.add("ONELETTER");
        featuresNames.add("ENDSWITHPERIOD");
        featuresNames.add("ENDSWITHCOMMA");

        SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(featuresNames);

        CRFByLabelLikelihoodTrainer crfTrainer = new CRFByLabelLikelihoodTrainer(serialPipes);

        crfTrainer.train(trainingFile, testingFile, true);

        System.out.println("Evaluation:");
        Evaluator crfEvaluator = new Evaluator(serialPipes);
        crfEvaluator.evaluate(crfTrainer.getTransducerTrainer(), testingFile);

    }

    private TransducerTrainer transducerTrainer;

    private SerialPipes serialPipes;

    public CRFByLabelLikelihoodTrainer(SerialPipes serialPipes) {
        this.serialPipes = serialPipes;
    }

    @Override
    public TransducerTrainer getTransducerTrainer() {
        return this.transducerTrainer;
    }

    @Override
    public void train(File trainingFile, File testingFile, boolean evaluateDuringTraining) {
        CRF crf = new CRF(this.serialPipes, null);

        InstanceList trainingInstances = InstanceListBuilder.build(trainingFile, this.serialPipes);
        InstanceList testingInstances = InstanceListBuilder.build(testingFile, this.serialPipes);

        // TODO handle states
        // crf.addStatesForLabelsConnectedAsIn(trainingInstances);
        crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        crf.addStartState();

        this.transducerTrainer = new CRFTrainerByLabelLikelihood(crf);
        ((CRFTrainerByLabelLikelihood) this.transducerTrainer).setGaussianPriorVariance(10.0);

        // TODO create methods for other CRF trainers

        // CRFTrainerByStochasticGradient trainer = new
        // CRFTrainerByStochasticGradient(crf, 1.0);

        // CRFTrainerByL1LabelLikelihood trainer = new
        // CRFTrainerByL1LabelLikelihood(crf, 0.75);

        if (evaluateDuringTraining) {
            this.transducerTrainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances, "training"));
            this.transducerTrainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
        }

        this.transducerTrainer.train(trainingInstances);
    }

}
