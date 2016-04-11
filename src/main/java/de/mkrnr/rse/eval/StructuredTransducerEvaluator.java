package de.mkrnr.rse.eval;

import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.InstanceList;

public abstract class StructuredTransducerEvaluator extends TransducerEvaluator {

    protected Evaluation evaluation;
    protected String otherLabel;

    public StructuredTransducerEvaluator(InstanceList[] instanceLists, String[] descriptions, String otherLabel) {
        super(instanceLists, descriptions);
        this.otherLabel = otherLabel;
    }

    @Override
    public abstract void evaluateInstanceList(TransducerTrainer transducer, InstanceList instances, String description);

    public Evaluation getEvaluation() {
        return this.evaluation;
    }

}
