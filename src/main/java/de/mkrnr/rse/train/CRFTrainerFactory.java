package de.mkrnr.rse.train;

import cc.mallet.fst.CRF;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.InstanceList;

public class CRFTrainerFactory extends TransducerTrainerFactory {
    private CRFBuilder crfBuilder;
    private TransducerTrainerBuilder transducerTrainerBuilder;

    public CRFTrainerFactory(CRFBuilder crfBuilder, TransducerTrainerBuilder transducerTrainerBuilder) {
        this.crfBuilder = crfBuilder;
        this.transducerTrainerBuilder = transducerTrainerBuilder;
    }

    @Override
    public TransducerTrainer getTransducerTrainer(InstanceList trainingInstances, InstanceList testingInstances,
            boolean evaluateDuringTraining) {

        CRF crf = this.crfBuilder.build(trainingInstances);

        TransducerTrainer transducerTrainer = this.transducerTrainerBuilder.build(crf);

        // TODO create methods for other CRF trainers

        // CRFTrainerByStochasticGradient trainer = new
        // CRFTrainerByStochasticGradient(crf, 1.0);

        // CRFTrainerByL1LabelLikelihood trainer = new
        // CRFTrainerByL1LabelLikelihood(crf, 0.75);

        if (evaluateDuringTraining) {
            transducerTrainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances, "training"));
            transducerTrainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
        }

        return transducerTrainer;

    }
}
