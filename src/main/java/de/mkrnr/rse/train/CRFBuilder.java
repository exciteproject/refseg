package de.mkrnr.rse.train;

import cc.mallet.fst.CRF;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;

public class CRFBuilder extends ConfigurableBuilder {
    private boolean addStatesForThreeQuarterLabelsConnected;
    private Pipe inputPipe;
    private Pipe outputPipe;

    public CRFBuilder(Pipe inputPipe, Pipe outputPipe) {
        this.inputPipe = inputPipe;
        this.outputPipe = outputPipe;
    }

    public CRF build(InstanceList trainingSet) {
        CRF crf = new CRF(this.inputPipe, this.outputPipe);

        if (this.addStatesForThreeQuarterLabelsConnected) {
            crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingSet);
        }

        crf.addStartState();
        return crf;
    }

    public Pipe getInputPipe() {
        return this.inputPipe;
    }

    public void setAddStatesForThreeQuarterLabelsConnected() {
        this.addStatesForThreeQuarterLabelsConnected = true;
    }

}
