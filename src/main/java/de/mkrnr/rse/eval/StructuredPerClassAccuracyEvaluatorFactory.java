package de.mkrnr.rse.eval;

import cc.mallet.types.InstanceList;

public class StructuredPerClassAccuracyEvaluatorFactory extends StructuredTransducerEvaluatorFactory {

    @Override
    public StructuredTransducerEvaluator getStructuredTransducerEvaluator(InstanceList testingInstances, String label) {
        return new StructuredPerClassAccuracyEvaluator(testingInstances, label);
    }

}
