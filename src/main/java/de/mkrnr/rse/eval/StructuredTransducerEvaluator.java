package de.mkrnr.rse.eval;

import java.util.HashSet;
import java.util.Set;

import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.InstanceList;

public abstract class StructuredTransducerEvaluator extends TransducerEvaluator {

    protected Evaluation evaluation;
    protected Set<String> otherLabels;

    public StructuredTransducerEvaluator(InstanceList[] instanceLists, String[] descriptions, String[] otherLabels) {
	super(instanceLists, descriptions);
	this.otherLabels = new HashSet<String>();
	for (String otherLabel : otherLabels) {
	    this.otherLabels.add(otherLabel);
	}
    }

    @Override
    public abstract void evaluateInstanceList(TransducerTrainer transducer, InstanceList instances, String description);

    public Evaluation getEvaluation() {
	return this.evaluation;
    }

}
