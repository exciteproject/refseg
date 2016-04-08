package de.mkrnr.rse.train;

import java.io.File;

import cc.mallet.fst.TransducerTrainer;

public interface Trainer {

    public TransducerTrainer getTransducerTrainer();

    public void train(File trainingFile, File testingFile, boolean evaluateDuringTraining);

}
