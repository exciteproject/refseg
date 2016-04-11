package de.mkrnr.rse.eval;

import cc.mallet.types.InstanceList;

public abstract class StructuredTransducerEvaluatorFactory {

    public abstract StructuredTransducerEvaluator getStructuredTransducerEvaluator(InstanceList testingInstances,
            String label, String otherLabel);

}
