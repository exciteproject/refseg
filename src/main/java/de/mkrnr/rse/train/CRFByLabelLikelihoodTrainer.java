package de.mkrnr.rse.train;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.pipe.SerialPipes;

public class CRFByLabelLikelihoodTrainer extends CRFTrainer {

    private Boolean usingHyperbolicPrior;
    private Double hyperbolicPriorSlope;
    private Double hyperbolicPriorSharpness;
    private Double gaussianPriorVariance;
    private Boolean useSparseWeights;

    public CRFByLabelLikelihoodTrainer(SerialPipes serialPipes) {
        super(serialPipes);
    }

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

    public void setGaussianPriorVariance(double p) {
        this.gaussianPriorVariance = p;
    }

    public void setHyperbolicPriorSharpness(double p) {
        this.hyperbolicPriorSharpness = p;
    }

    public void setHyperbolicPriorSlope(double p) {
        this.hyperbolicPriorSlope = p;
    }

    public void setUseHyperbolicPrior(boolean f) {
        this.usingHyperbolicPrior = f;
    }

    public void setUseSparseWeights(boolean b) {
        this.useSparseWeights = b;
    }

    @Override
    protected void setTransducerTrainer(CRF crf) {
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
        if (this.usingHyperbolicPrior != null) {
            crfTrainerByLabelLikelihood.setUseHyperbolicPrior(this.usingHyperbolicPrior);
        }

        this.transducerTrainer = crfTrainerByLabelLikelihood;
    }

}
