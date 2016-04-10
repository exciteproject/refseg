package de.mkrnr.rse.train;

import cc.mallet.fst.TransducerTrainer;
import cc.mallet.types.InstanceList;

public abstract class TransducerTrainerFactory {

    public abstract TransducerTrainer getTransducerTrainer(InstanceList trainingInstances,
            InstanceList testingInstances, boolean evaluateDuringTraining);

}
