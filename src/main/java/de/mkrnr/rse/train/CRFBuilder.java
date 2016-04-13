package de.mkrnr.rse.train;

import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.util.Configuration;

public class CRFBuilder extends ConfigurableBuilder {

    protected static final String ADD_STATES_FOR_THREE_QUARTER_LABELS_CONNECTED = "addStatesForThreeQuarterLabelsConnected";

    protected static final String ADD_FULLY_CONNECTED_STATES_FOR_BI_LABELS = "addFullyConnectedStatesForBiLabels";

    protected static final String ADD_FULLY_CONNECTED_STATES_FOR_TRI_LABELS = "addFullyConnectedStatesForTriLabels";

    protected static final String ADD_ORDER_N_STATES = "addOrderNStates";

    protected static final String ADD_FULLY_CONNECTED_STATES_FOR_LABELS = "addFullyConnectedStatesForLabels";

    protected static final String ADD_STATES_FOR_LABELS_CONNECTED = "addStatesForLabelsConnected";

    protected static final String ADD_FULLY_CONNECTED_STATES_FOR_THREE_QUATER_LABELS = "addFullyConnectedStatesForThreeQuarterLabels";

    protected static final String ADD_STATES_FOR_BI_LABELS_CONNECTED = "addStatesForBiLabelsConnected";

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
            case ADD_STATES_FOR_LABELS_CONNECTED:
                crf.addStatesForLabelsConnectedAsIn(trainingSet);
                break;
            case ADD_STATES_FOR_BI_LABELS_CONNECTED:
                crf.addStatesForBiLabelsConnectedAsIn(trainingSet);
                break;
            case ADD_FULLY_CONNECTED_STATES_FOR_BI_LABELS:
                crf.addFullyConnectedStatesForBiLabels();
                break;
            case ADD_ORDER_N_STATES:
                int[] orders = { 1 };
                crf.addOrderNStates(trainingSet, orders, null, null, null, null, false);
                break;
            case ADD_FULLY_CONNECTED_STATES_FOR_LABELS:
                crf.addFullyConnectedStatesForLabels();
                break;
            case ADD_FULLY_CONNECTED_STATES_FOR_THREE_QUATER_LABELS:
                crf.addFullyConnectedStatesForThreeQuarterLabels(trainingSet);
                break;
            case ADD_FULLY_CONNECTED_STATES_FOR_TRI_LABELS:
                crf.addFullyConnectedStatesForTriLabels();
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
