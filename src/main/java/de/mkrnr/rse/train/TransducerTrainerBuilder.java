package de.mkrnr.rse.train;

import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.fst.TransducerTrainer;
import de.mkrnr.rse.util.Configuration;

public abstract class TransducerTrainerBuilder extends ConfigurableBuilder {

    public TransducerTrainerBuilder(List<Configuration> configurations) {
        super(configurations);
    }

    protected abstract TransducerTrainer build(CRF crf);

}
