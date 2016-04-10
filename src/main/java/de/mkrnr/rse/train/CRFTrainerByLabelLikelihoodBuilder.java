package de.mkrnr.rse.train;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;

public class CRFTrainerByLabelLikelihoodBuilder extends TransducerTrainerBuilder {

    private Double gaussianPriorVariance;
    private Double hyperbolicPriorSharpness;
    private Double hyperbolicPriorSlope;
    private Boolean useSparseWeights;
    private Boolean useHyperbolicPrior;

    public double getGaussianPriorVariance() {
        return this.gaussianPriorVariance;
    }

    // public int getDefaultFeatureIndex () { return defaultFeatureIndex;}
    public double getUseHyperbolicPriorSharpness() {
        return this.hyperbolicPriorSharpness;
    }

    public double getUseHyperbolicPriorSlope() {
        return this.hyperbolicPriorSlope;
    }

    public boolean getUseSparseWeights() {
        return this.useSparseWeights;
    }

    public void setGaussianPriorVariance(double gaussianPriorVariance) {
        this.gaussianPriorVariance = gaussianPriorVariance;
    }

    public void setHyperbolicPriorSharpness(double hyperbolicPriorSharpness) {
        this.hyperbolicPriorSharpness = hyperbolicPriorSharpness;
    }

    public void setHyperbolicPriorSlope(double hyperbolicPriorSlope) {
        this.hyperbolicPriorSlope = hyperbolicPriorSlope;
    }

    public void setUseHyperbolicPrior(boolean useHyperbolicPrior) {
        this.useHyperbolicPrior = useHyperbolicPrior;
    }

    public void setUseSparseWeights(boolean useSparseWeights) {
        this.useSparseWeights = useSparseWeights;
    }

    protected CRFTrainerByLabelLikelihood build(CRF crf) {
        CRFTrainerByLabelLikelihood crfTrainerByLabelLikelihood = new CRFTrainerByLabelLikelihood(crf);

        if (this.gaussianPriorVariance != null) {
            crfTrainerByLabelLikelihood.setGaussianPriorVariance(this.gaussianPriorVariance);
        }
        if (this.hyperbolicPriorSharpness != null) {
            crfTrainerByLabelLikelihood.setHyperbolicPriorSharpness(this.hyperbolicPriorSharpness);
        }
        if (this.hyperbolicPriorSlope != null) {
            crfTrainerByLabelLikelihood.setHyperbolicPriorSlope(this.hyperbolicPriorSlope);
        }
        if (this.useSparseWeights != null) {
            crfTrainerByLabelLikelihood.setUseSparseWeights(this.useSparseWeights);
        }
        if (this.useHyperbolicPrior != null) {
            crfTrainerByLabelLikelihood.setUseHyperbolicPrior(this.useHyperbolicPrior);
        }

        return crfTrainerByLabelLikelihood;
    }

}
