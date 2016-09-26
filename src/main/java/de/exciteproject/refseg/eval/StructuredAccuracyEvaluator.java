package de.exciteproject.refseg.eval;

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
public class StructuredAccuracyEvaluator extends StructuredTransducerEvaluator {

    public StructuredAccuracyEvaluator(InstanceList i1, String d1, String[] otherLabels) {
	this(new InstanceList[] { i1 }, new String[] { d1 }, otherLabels);
    }

    private StructuredAccuracyEvaluator(InstanceList[] instanceLists, String[] descriptions, String[] otherLabels) {
	super(instanceLists, descriptions, otherLabels);
    }

    @Override
    public void evaluateInstanceList(TransducerTrainer tt, InstanceList data, String description) {
	Evaluation evaluation = new Evaluation();

	// add general information on this iteration
	evaluation.setIteration(tt.getIteration());
	evaluation.setDescription(description);

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

	int totalNumCorrectTokens = 0;
	int totalNumPredTokens = 0;
	int totalNumTrueTokens = 0;

	int labelsNumCorrectTokens = 0;
	int labelsNumPredTokens = 0;
	int labelsNumTrueTokens = 0;

	for (int i = 0; i < numLabels; i++) {
	    String label = dict.lookupObject(i).toString();

	    Double precision = this.getPrecision(numCorrectTokens[i], numTrueTokens[i], numPredTokens[i]);
	    Double recall = this.getRecall(numCorrectTokens[i], numTrueTokens[i], numPredTokens[i]);
	    Double f1 = this.getF1(precision, recall);

	    this.addAccuracy(label, precision, recall, f1, evaluation);
	    this.addCounts(label, numCorrectTokens[i], numPredTokens[i], numTrueTokens[i], evaluation);

	    if (!this.otherLabels.contains(label.toString())) {
		labelsNumCorrectTokens += numCorrectTokens[i];
		labelsNumPredTokens += numPredTokens[i];
		labelsNumTrueTokens += numTrueTokens[i];
	    }

	    totalNumCorrectTokens += numCorrectTokens[i];
	    totalNumPredTokens += numPredTokens[i];
	    totalNumTrueTokens += numTrueTokens[i];
	}

	this.calculateAndAddAccuracy("total", totalNumCorrectTokens, totalNumPredTokens, totalNumTrueTokens,
		evaluation);
	this.addCounts("total", totalNumCorrectTokens, totalNumPredTokens, totalNumTrueTokens, evaluation);
	this.calculateAndAddAccuracy("labels", labelsNumCorrectTokens, labelsNumPredTokens, labelsNumTrueTokens,
		evaluation);
	this.addCounts("labels", labelsNumCorrectTokens, labelsNumPredTokens, labelsNumTrueTokens, evaluation);

	this.evaluations.add(evaluation);
	System.out.println(evaluation);
    }

    private void addAccuracy(String label, Double precision, Double recall, Double f1, Evaluation evaluation) {

	if (!precision.isNaN()) {
	    evaluation.addEvaluationResult(label, "precision", precision);
	}
	if (!recall.isNaN()) {
	    evaluation.addEvaluationResult(label, "recall", recall);
	}
	if (!f1.isNaN()) {
	    evaluation.addEvaluationResult(label, "f1", f1);
	}

    }

    private void addCounts(String label, int numCorrectTokens, int numPredTokens, int numTrueTokens,
	    Evaluation evaluation) {
	evaluation.addEvaluationResult(label, "tokenCorrect", numCorrectTokens);
	evaluation.addEvaluationResult(label, "tokenPredicted", numPredTokens);
	evaluation.addEvaluationResult(label, "tokenTrue", numTrueTokens);
    }

    private void calculateAndAddAccuracy(String label, int numCorrectTokens, int numPredTokens, int numTrueTokens,
	    Evaluation evaluation) {

	Double precision = this.getPrecision(numCorrectTokens, numTrueTokens, numPredTokens);
	Double recall = this.getRecall(numCorrectTokens, numTrueTokens, numPredTokens);
	Double f1 = this.getF1(precision, recall);

	this.addAccuracy(label, precision, recall, f1, evaluation);
    }

    private Double getF1(Double precision, Double recall) {
	Double f1 = (2 * precision * recall) / (precision + recall);

	if (f1.isNaN()) {
	    if ((precision == 0.0) && (recall == 0.0)) {
		f1 = 0.0;
	    }
	}
	return f1;
    }

    private Double getPrecision(int numCorrectTokens, int numTrueTokens, int numPredTokens) {
	Double precision = ((double) numCorrectTokens) / numPredTokens;
	if (precision.isNaN()) {
	    // case 1: numTrueTokens>0: precision = 0 since no token was
	    // found
	    if (numTrueTokens > 0) {
		precision = 0.0;
	    }
	    // case 2: numTrueTokens=0: ignore
	    // the result (leave it NaN)
	}
	return precision;
    }

    private Double getRecall(int numCorrectTokens, int numTrueTokens, int numPredTokens) {

	Double recall = ((double) numCorrectTokens) / numTrueTokens;

	if (recall.isNaN()) {
	    // case 1: numPredTokens>0: recall = 0 since the recall is zero
	    // for the found tokens
	    if (numPredTokens > 0) {
		recall = 0.0;
	    }
	    // case 2: numPredTokens=0: ignore
	    // the result (leave it NaN)
	}

	return recall;
    }
}
