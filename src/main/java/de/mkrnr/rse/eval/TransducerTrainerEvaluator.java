package de.mkrnr.rse.eval;

import java.io.File;

import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.fst.ViterbiWriter;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.util.InstanceListBuilder;

public class TransducerTrainerEvaluator {

    private SerialPipes serialPipes;

    private TransducerTrainer trainer;

    public TransducerTrainerEvaluator(SerialPipes serialPipes, TransducerTrainer trainer) {
        this.serialPipes = serialPipes;
        this.trainer = trainer;
    }

    public Evaluation evaluate(File trainingFile, File testingFile) {
        Evaluation evaluation = new Evaluation();
        // TODO store results in evaluation
        InstanceList trainingInstances = InstanceListBuilder.build(trainingFile, this.serialPipes);
        InstanceList testingInstances = InstanceListBuilder.build(testingFile, this.serialPipes);

        // TODO Write new evaluators that return their results
        PerClassAccuracyEvaluator trainingPerClassAccuracyEvaluator = new PerClassAccuracyEvaluator(trainingInstances,
                "training");
        trainingPerClassAccuracyEvaluator.evaluate(this.trainer);

        PerClassAccuracyEvaluator testingPerClassAccuracyEvaluator = new PerClassAccuracyEvaluator(testingInstances,
                "testing");
        testingPerClassAccuracyEvaluator.evaluate(this.trainer);

        // TokenAccuracyEvaluator testingTokenAccuracyEvaluator = new
        // TokenAccuracyEvaluator(this.testingInstances,
        // "testing");
        // testingTokenAccuracyEvaluator.evaluate(this.trainer);

        ViterbiWriter viterbiWriter = new ViterbiWriter("output/ner_crf",
                new InstanceList[] { trainingInstances, testingInstances }, new String[] { "train", "test" });
        viterbiWriter.evaluate(this.trainer);
        return evaluation;
    }

}
