package de.mkrnr.rse.train;

import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import de.mkrnr.rse.util.Configuration;

public class CRFTrainerByLabelLikelihoodBuilder extends TransducerTrainerBuilder {

    protected static final String GAUSSIAN_PRIOR_VARIANCE = "gaussianPriorVariance";
    protected static final String HYPERBOLIC_PRIOR_SHARPNESS = "hyperbolicPriorSharpness";
    protected static final String HYPERBOLIC_PRIOR_SLOPE = "hyperbolicPriorSlope";
    protected static final String USE_SPARSE_WEIGHTS = "useSparseWeights";
    protected static final String USE_HYPERBOLIC_PRIOR = "useHyperbolicPrior";

    public CRFTrainerByLabelLikelihoodBuilder(List<Configuration> configurations) {
        super(configurations);
    }

    @Override
    protected CRFTrainerByLabelLikelihood build(CRF crf) {
        CRFTrainerByLabelLikelihood crfTrainerByLabelLikelihood = new CRFTrainerByLabelLikelihood(crf);

        for (Configuration configuration : this.configurations) {
            switch (configuration.getName()) {

            case GAUSSIAN_PRIOR_VARIANCE:
                crfTrainerByLabelLikelihood.setGaussianPriorVariance(configuration.getDoubleValue());
                break;
            case HYPERBOLIC_PRIOR_SHARPNESS:
                crfTrainerByLabelLikelihood.setHyperbolicPriorSharpness(configuration.getDoubleValue());
                break;
            case HYPERBOLIC_PRIOR_SLOPE:
                crfTrainerByLabelLikelihood.setHyperbolicPriorSlope(configuration.getDoubleValue());
                break;
            case USE_SPARSE_WEIGHTS:
                crfTrainerByLabelLikelihood.setUseSparseWeights(configuration.getBooleanValue());
                break;
            case USE_HYPERBOLIC_PRIOR:
                crfTrainerByLabelLikelihood.setUseHyperbolicPrior(configuration.getBooleanValue());
                break;
            default:
                throw new IllegalArgumentException(
                        "configuration not valid for CRFTrainerByLabelLikelihood: " + configuration.getName());

            }
        }
        return crfTrainerByLabelLikelihood;
    }

}
