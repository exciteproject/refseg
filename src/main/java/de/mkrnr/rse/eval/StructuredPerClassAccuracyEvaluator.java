package de.mkrnr.rse.eval;

import java.util.ArrayList;

import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

/**
 * Code taken from cc.mallet.fst.PerClassAccuracyEvaluator. Added storage of
 * results into Evaluation
 */
public class StructuredPerClassAccuracyEvaluator extends StructuredTransducerEvaluator {

    public StructuredPerClassAccuracyEvaluator(InstanceList i1, String d1) {
        this(new InstanceList[] { i1 }, new String[] { d1 });
    }

    private StructuredPerClassAccuracyEvaluator(InstanceList[] instanceLists, String[] descriptions) {
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

        ArrayList<Double> allF1 = new ArrayList<Double>();

        for (int i = 0; i < numLabels; i++) {
            Object label = dict.lookupObject(i);
            Double precision = ((double) numCorrectTokens[i]) / numPredTokens[i];
            Double recall = ((double) numCorrectTokens[i]) / numTrueTokens[i];

            if (Double.isNaN(precision)) {
                // cases: numTrueTokens>0: precision = 0 since no token was
                // found
                // numTrueTokens=0: ignore
                // the result
                if (numTrueTokens[i] > 0) {
                    precision = 0.0;
                }
            }

            Double f1 = (2 * precision * recall) / (precision + recall);

            if (f1.isNaN()) {
                if (!precision.isNaN() && !recall.isNaN()) {
                    f1 = 0.0;
                }
            }
            if (!f1.isNaN()) {
                // only happens when recall=NaN and precision = NaN
                allF1.add(f1);
            }

            // if (recall.isNaN()) {
            // //either recall recall = 1 (kind grader) or ignore result
            // //recall = 0.0;
            // }

            if (!precision.isNaN()) {
                this.evaluation.addEvaluationResult(label + " precision", precision);
            }
            if (!recall.isNaN()) {
                this.evaluation.addEvaluationResult(label + " recall", recall);
            }
            if (!f1.isNaN()) {
                this.evaluation.addEvaluationResult(label + " f1", f1);
            }
        }

        Double aggregatedF1 = 0.0;
        for (Double f1 : allF1) {

            aggregatedF1 += f1;
        }
        Double f1Mean = aggregatedF1 / allF1.size();
        if (!f1Mean.isNaN()) {
            this.evaluation.addEvaluationResult("mean f1 ", f1Mean);
        }

    }
}
