package de.mkrnr.rse.eval;

import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.MatrixOps;
import cc.mallet.types.Sequence;

/**
 * Code taken from cc.mallet.fst.PerClassAccuracyEvaluator. Added storage of
 * results into Evaluation
 */
public class PerClassAccuracyEvaluator extends TransducerEvaluator {

    private Evaluation evaluation;

    public PerClassAccuracyEvaluator(InstanceList i1, String d1) {
        this(new InstanceList[] { i1 }, new String[] { d1 });
    }

    private PerClassAccuracyEvaluator(InstanceList[] instanceLists, String[] descriptions) {
        super(instanceLists, descriptions);
    }

    @Override
    public void evaluateInstanceList(TransducerTrainer tt, InstanceList data, String description) {
        this.evaluation = new Evaluation();
        Transducer model = tt.getTransducer();
        Alphabet dict = model.getInputPipe().getTargetAlphabet();
        int numLabels = dict.size();
        int[] numCorrectTokens = new int[numLabels];
        int[] numPredTokens = new int[numLabels];
        int[] numTrueTokens = new int[numLabels];

        for (int i = 0; i < data.size(); i++) {
            Instance instance = data.get(i);
            Sequence<?> input = (Sequence<?>) instance.getData();
            Sequence<?> trueOutput = (Sequence<?>) instance.getTarget();
            assert (input.size() == trueOutput.size());
            Sequence<?> predOutput = model.transduce(input);
            assert (predOutput.size() == trueOutput.size());
            for (int j = 0; j < trueOutput.size(); j++) {
                int idx = dict.lookupIndex(trueOutput.get(j));
                numTrueTokens[idx]++;
                numPredTokens[dict.lookupIndex(predOutput.get(j))]++;
                if (trueOutput.get(j).equals(predOutput.get(j))) {
                    numCorrectTokens[idx]++;
                }
            }
        }

        double[] allf = new double[numLabels];
        for (int i = 0; i < numLabels; i++) {
            Object label = dict.lookupObject(i);
            double precision = ((double) numCorrectTokens[i]) / numPredTokens[i];
            double recall = ((double) numCorrectTokens[i]) / numTrueTokens[i];
            double f1 = (2 * precision * recall) / (precision + recall);
            if (!Double.isNaN(f1)) {
                allf[i] = f1;
            }

            // TODO remove this
            if (Double.isNaN(precision)) {
                precision = 0.0;
            }
            if (Double.isNaN(recall)) {
                recall = 0.0;
            }
            if (Double.isNaN(f1)) {
                f1 = 0.0;
            }

            this.evaluation.addEvaluationResult(label + " precision", precision);
            this.evaluation.addEvaluationResult(label + " recall", recall);
            this.evaluation.addEvaluationResult(label + " f1", f1);
        }

        this.evaluation.addEvaluationResult("average f1 ", MatrixOps.mean(allf));

    }

    public Evaluation getEvaluation() {
        return this.evaluation;
    }

}
