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

    public StructuredPerClassAccuracyEvaluator(InstanceList i1, String d1, String[] otherLabels) {
	this(new InstanceList[] { i1 }, new String[] { d1 }, otherLabels);
    }

    private StructuredPerClassAccuracyEvaluator(InstanceList[] instanceLists, String[] descriptions,
	    String[] otherLabels) {
	super(instanceLists, descriptions, otherLabels);
    }

    @Override
    public void evaluateInstanceList(TransducerTrainer tt, InstanceList data, String description) {
	Evaluation evaluation = new Evaluation();

	// add general information on this iteration
	evaluation.setIteration(tt.getIteration());

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

	int totalNumCorrectTokens = 0;
	int totalNumPredTokens = 0;
	int totalNumTrueTokens = 0;

	for (int i = 0; i < numLabels; i++) {
	    Object label = dict.lookupObject(i);
	    Double precision = ((double) numCorrectTokens[i]) / numPredTokens[i];
	    Double recall = ((double) numCorrectTokens[i]) / numTrueTokens[i];

	    if (precision.isNaN()) {
		// case 1: numTrueTokens>0: precision = 0 since no token was
		// found
		if (numTrueTokens[i] > 0) {
		    precision = 0.0;
		}
		// case 2: numTrueTokens=0: ignore
		// the result (leave it NaN)
	    }

	    if (recall.isNaN()) {
		// case 1: numPredTokens>0: recall = 0 since the recall is zero
		// for the found tokens
		if (numPredTokens[i] > 0) {
		    recall = 0.0;
		}
		// case 2: numPredTokens=0: ignore
		// the result (leave it NaN)
	    }

	    Double f1 = (2 * precision * recall) / (precision + recall);

	    if (f1.isNaN()) {
		if ((precision == 0.0) && (recall == 0.0)) {
		    f1 = 0.0;
		}
	    }
	    if (!f1.isNaN()) {
		// only happens when recall>=0 and precision>=0
		allF1.add(f1);
	    }

	    if (!precision.isNaN()) {
		evaluation.addEvaluationResult(label.toString(), "precision", precision);
	    }
	    if (!recall.isNaN()) {
		evaluation.addEvaluationResult(label.toString(), "recall", recall);
	    }
	    if (!f1.isNaN()) {
		evaluation.addEvaluationResult(label.toString(), "f1", f1);
	    }

	    if (!this.otherLabels.contains(label.toString())) {
		totalNumCorrectTokens += numCorrectTokens[i];
		totalNumPredTokens += numPredTokens[i];
		totalNumTrueTokens += numTrueTokens[i];
	    }
	}

	Double aggregatedF1 = 0.0;
	for (Double f1 : allF1) {

	    aggregatedF1 += f1;
	}
	Double f1Mean = aggregatedF1 / allF1.size();
	if (!f1Mean.isNaN()) {
	    evaluation.addEvaluationResult("mean", "f1", f1Mean);
	}

	Double totalPrecision = ((double) totalNumCorrectTokens) / totalNumPredTokens;
	Double totalRecall = ((double) totalNumCorrectTokens) / totalNumTrueTokens;
	Double totalF1 = (2 * totalPrecision * totalRecall) / (totalPrecision + totalRecall);

	if (!totalPrecision.isNaN()) {
	    evaluation.addEvaluationResult("total", "precision", totalPrecision);
	}
	if (!totalRecall.isNaN()) {
	    evaluation.addEvaluationResult("total", "recall", totalRecall);
	}
	if (!totalF1.isNaN()) {
	    evaluation.addEvaluationResult("total", "f1", totalF1);
	}
	this.evaluations.add(evaluation);
	evaluation.printEvaluationResults();
    }
}
