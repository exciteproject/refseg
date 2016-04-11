package de.mkrnr.rse.train;

import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.util.Configuration;

public class CRFBuilder extends ConfigurableBuilder {

    protected static final String ADD_STATES_FOR_THREE_QUARTER_LABELS_CONNECTED = "addStatesForThreeQuarterLabelsConnected";

    private Pipe inputPipe;
    private Pipe outputPipe;

    public CRFBuilder(List<Configuration> configurations, Pipe inputPipe, Pipe outputPipe) {
        super(configurations);
        this.inputPipe = inputPipe;
        this.outputPipe = outputPipe;
    }

    public CRF build(InstanceList trainingSet) {
        CRF crf = new CRF(this.inputPipe, this.outputPipe);

        for (Configuration configuration : this.configurations) {
            switch (configuration.getName()) {
            case ADD_STATES_FOR_THREE_QUARTER_LABELS_CONNECTED:
                crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingSet);
                break;
            }
        }

        crf.addStartState();
        return crf;
    }

    public Pipe getInputPipe() {
        return this.inputPipe;
    }

}
