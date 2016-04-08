package de.mkrnr.rse.eval;

import java.io.File;

import cc.mallet.fst.TransducerTrainer;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.util.InstanceListBuilder;

public class Evaluator {

    private SerialPipes serialPipes;

    public Evaluator(SerialPipes serialPipes) {
        this.serialPipes = serialPipes;
    }

    public Evaluation evaluate(TransducerTrainer transducerTrainer, File testingFile) {
        InstanceList testingInstances = InstanceListBuilder.build(testingFile, this.serialPipes);

        PerClassAccuracyEvaluator testingPerClassAccuracyEvaluator = new PerClassAccuracyEvaluator(testingInstances,
                "testing");
        testingPerClassAccuracyEvaluator.evaluate(transducerTrainer);

        return testingPerClassAccuracyEvaluator.getEvaluation();

        // TokenAccuracyEvaluator testingTokenAccuracyEvaluator = new
        // TokenAccuracyEvaluator(this.testingInstances,
        // "testing");
        // testingTokenAccuracyEvaluator.evaluate(this.trainer);

        // ViterbiWriter viterbiWriter = new ViterbiWriter("output/ner_crf",
        // new InstanceList[] { trainingInstances, testingInstances }, new
        // String[] { "train", "test" });
        // viterbiWriter.evaluate(transducerTrainer);
    }

}
