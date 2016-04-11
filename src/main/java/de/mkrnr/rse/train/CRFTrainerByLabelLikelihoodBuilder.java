package de.mkrnr.rse.train;

import java.util.List;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;

public class CRFTrainerByLabelLikelihoodBuilder extends TransducerTrainerBuilder {

    static final String GAUSSIAN_PRIOR_VARIANCE = "gaussianPriorVariance";
    static final String HYPERBOLIC_PRIOR_SHARPNESS = "hyperbolicPriorSharpness";
    static final String HYPERBOLIC_PRIOR_SLOPE = "hyperbolicPriorSlope";
    static final String USE_SPARSE_WEIGHTS = "useSparseWeights";
    static final String USE_HYPERBOLIC_PRIOR = "useHyperbolicPrior";

    public static void main(String[] args) {
        CRFTrainerByLabelLikelihoodBuilder crfTrainerByLabelLikelihoodBuilder = new CRFTrainerByLabelLikelihoodBuilder();
        List<String> possibleConfiguration = crfTrainerByLabelLikelihoodBuilder.getPossibleConfigurations();
        System.out.println(possibleConfiguration.get(0));
    }

    public CRFTrainerByLabelLikelihoodBuilder() {
        super();
    }

    @Override
    protected CRFTrainerByLabelLikelihood build(CRF crf) {
        CRFTrainerByLabelLikelihood crfTrainerByLabelLikelihood = new CRFTrainerByLabelLikelihood(crf);

        for (Configuration configuration : this.configurations.getConfigurations()) {
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
