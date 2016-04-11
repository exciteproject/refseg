package de.mkrnr.rse.train;

import cc.mallet.fst.CRF;
import cc.mallet.fst.TransducerTrainer;

public abstract class TransducerTrainerBuilder extends ConfigurableBuilder {

    public TransducerTrainerBuilder() {
        super();
    }

    protected abstract TransducerTrainer build(CRF crf);

}
