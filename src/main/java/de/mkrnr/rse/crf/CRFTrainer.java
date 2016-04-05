package de.mkrnr.rse.crf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.eval.TransducerTrainerEvaluator;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.util.InstanceListBuilder;

public class CRFTrainer {

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

        CRFTrainer crfTrainer = new CRFTrainer(serialPipes);

        crfTrainer.trainByLabelLikelihood(trainingFile, testingFile, true);

        System.out.println("Evaluation:");
        TransducerTrainerEvaluator crfEvaluator = new TransducerTrainerEvaluator(serialPipes, crfTrainer.getTrainer());
        crfEvaluator.evaluate(trainingFile, testingFile);

    }

    private TransducerTrainer trainer;

    private SerialPipes serialPipes;

    public CRFTrainer(SerialPipes serialPipes) {
        this.serialPipes = serialPipes;
    }

    public TransducerTrainer getTrainer() {
        return this.trainer;
    }

    public void trainByLabelLikelihood(File trainingFile, File testingFile, boolean evaluationDuringTraining) {
        CRF crf = new CRF(this.serialPipes, null);

        InstanceList trainingInstances = InstanceListBuilder.build(trainingFile, this.serialPipes);
        InstanceList testingInstances = InstanceListBuilder.build(testingFile, this.serialPipes);

        // TODO handle states
        // crf.addStatesForLabelsConnectedAsIn(trainingInstances);
        crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        crf.addStartState();

        this.trainer = new CRFTrainerByLabelLikelihood(crf);
        ((CRFTrainerByLabelLikelihood) this.trainer).setGaussianPriorVariance(10.0);

        // TODO create methods for other CRF trainers

        // CRFTrainerByStochasticGradient trainer = new
        // CRFTrainerByStochasticGradient(crf, 1.0);

        // CRFTrainerByL1LabelLikelihood trainer = new
        // CRFTrainerByL1LabelLikelihood(crf, 0.75);

        if (evaluationDuringTraining) {
            this.trainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances, "training"));
            this.trainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
        }

        this.trainer.train(trainingInstances);
    }

}
