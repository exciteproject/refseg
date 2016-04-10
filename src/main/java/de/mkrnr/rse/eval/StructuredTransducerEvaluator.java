package de.mkrnr.rse.eval;

import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.InstanceList;

public abstract class StructuredTransducerEvaluator extends TransducerEvaluator {

    protected Evaluation evaluation;

    public StructuredTransducerEvaluator(InstanceList[] instanceLists, String[] descriptions) {
        super(instanceLists, descriptions);
    }

    @Override
    public abstract void evaluateInstanceList(TransducerTrainer transducer, InstanceList instances, String description);

    public Evaluation getEvaluation() {
        return this.evaluation;
    }

}
