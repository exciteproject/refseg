package de.mkrnr.rse.train;

import java.io.File;

import cc.mallet.fst.CRF;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.util.InstanceListBuilder;

public abstract class CRFTrainer implements Trainer {
    private SerialPipes serialPipes;
    protected TransducerTrainer transducerTrainer;

    private boolean addStatesForThreeQuarterLabelsConnected;

    public CRFTrainer(SerialPipes serialPipes) {
        this.serialPipes = serialPipes;
    }

    @Override
    public TransducerTrainer getTransducerTrainer() {
        return this.transducerTrainer;
    }

    public void setAddStatesForThreeQuarterLabelsConnected() {
        this.addStatesForThreeQuarterLabelsConnected = true;
    }

    @Override
    public void train(File trainingFile, File testingFile, boolean evaluateDuringTraining) {
        InstanceList trainingInstances = InstanceListBuilder.build(trainingFile, this.serialPipes);
        InstanceList testingInstances = InstanceListBuilder.build(testingFile, this.serialPipes);

        CRF crf = new CRF(this.serialPipes, null);
        // TODO add more states
        if (this.addStatesForThreeQuarterLabelsConnected) {
            crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        }

        crf.addStartState();

        this.setTransducerTrainer(crf);

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

    protected abstract void setTransducerTrainer(CRF crf);

}
