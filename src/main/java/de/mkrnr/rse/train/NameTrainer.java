package de.mkrnr.rse.train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.fst.semi_supervised.CRFTrainerByGE;
import cc.mallet.fst.semi_supervised.FSTConstraintUtil;
import cc.mallet.fst.semi_supervised.constraints.GEConstraint;
import cc.mallet.fst.semi_supervised.constraints.OneLabelKLGEConstraints;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
import de.mkrnr.rse.eval.EvaluationResults;
import de.mkrnr.rse.util.Configuration;

public class NameTrainer {

    public static final String ADD_ORDER_N_STATES_CONFIG_LABEL = "addOrderNStates";
    public static final String GAUSSIAN_PRIOR_VARIANCE_CONFIG_LABEL = "gaussianPriorVariance";
    public static final String NUM_ITERATIONS_CONFIG_LABEL = "numIterations";
    public static final String NUM_RESETS_CONFIG_LABEL = "numResets";
    public static final String NUM_THREADS_CONFIG_LABEL = "numThreads";
    private double gaussianPriorVariance;
    private int numIterations;
    private int numResets;
    private int numTreads;
    private boolean addOrderNStates;

    public TransducerTrainer train(InstanceList trainingInstances, InstanceList testingInstances,
	    File nameConstraintFile, List<Configuration> trainerConfigurations,
	    List<TransducerEvaluator> trainingEvaluators, EvaluationResults evaluationResults) throws IOException {

	// set default values
	this.gaussianPriorVariance = 10.0;
	this.numIterations = 1000;
	this.numResets = 4;
	this.numTreads = 1;

	// load configurations
	this.setConfigurations(trainerConfigurations);

	// TODO add parameters
	Pattern forbiddenPat = Pattern.compile("\\s");
	Pattern allowedPat = Pattern.compile(".*");

	CRF crf = new CRF(trainingInstances.getPipe(), (Pipe) null);
	if (this.addOrderNStates) {
	    String startName = crf.addOrderNStates(trainingInstances, new int[] { 1 }, null, "O", forbiddenPat,
		    allowedPat, true);
	    for (int i = 0; i < crf.numStates(); i++) {
		crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
	    }
	    crf.getState(startName).setInitialWeight(0.0);

	}
	crf.setWeightsDimensionDensely();

	ArrayList<GEConstraint> constraintsList = this.getKLGEConstraints(nameConstraintFile, trainingInstances);

	CRFTrainerByGE trainer = new CRFTrainerByGE(crf, constraintsList, this.numTreads);
	trainer.setGaussianPriorVariance(this.gaussianPriorVariance);
	trainer.setNumResets(this.numResets);

	for (TransducerEvaluator trainingEvaluator : trainingEvaluators) {
	    trainer.addEvaluator(trainingEvaluator);
	}

	Instant start = Instant.now();
	boolean converged = trainer.train(trainingInstances, this.numIterations);
	Instant end = Instant.now();

	evaluationResults.setTimeInMillis(Duration.between(start, end).toMillis());

	evaluationResults.setIterations(trainer.getIteration());
	evaluationResults.setConverged(converged);

	evaluationResults.addConfiguration(GAUSSIAN_PRIOR_VARIANCE_CONFIG_LABEL, this.gaussianPriorVariance);
	evaluationResults.addConfiguration(NUM_ITERATIONS_CONFIG_LABEL, this.numIterations);
	evaluationResults.addConfiguration(NUM_RESETS_CONFIG_LABEL, this.numResets);
	evaluationResults.addConfiguration(NUM_THREADS_CONFIG_LABEL, this.numTreads);
	return trainer;
    }

    private ArrayList<GEConstraint> getKLGEConstraints(File nameConstraintFile, InstanceList trainingInstances)
	    throws FileNotFoundException {
	HashMap<Integer, double[][]> constraints = FSTConstraintUtil
		.loadGEConstraints(new FileReader(nameConstraintFile), trainingInstances);

	ArrayList<GEConstraint> constraintsList = new ArrayList<GEConstraint>();

	OneLabelKLGEConstraints geConstraints = new OneLabelKLGEConstraints();
	for (int fi : constraints.keySet()) {
	    double[][] dist = constraints.get(fi);

	    boolean allSame = true;
	    double sum = 0;

	    double[] prob = new double[dist.length];
	    for (int li = 0; li < dist.length; li++) {
		prob[li] = dist[li][0];
		if (!Maths.almostEquals(dist[li][0], dist[li][1])) {
		    allSame = false;
		    break;
		} else if (Double.isInfinite(prob[li])) {
		    prob[li] = 0;
		}
		sum += prob[li];
	    }

	    if (!allSame) {
		throw new RuntimeException("A KL divergence penalty cannot be used with target ranges!");
	    }
	    if (!Maths.almostEquals(sum, 1)) {
		throw new RuntimeException("Targets must sum to 1 when using a KL divergence penalty!");
	    }

	    geConstraints.addConstraint(fi, prob, 1);
	}
	constraintsList.add(geConstraints);
	return constraintsList;
    }

    private void setConfigurations(List<Configuration> configurations) {

	for (Configuration configuration : configurations) {
	    switch (configuration.getName()) {
	    case NameTrainer.ADD_ORDER_N_STATES_CONFIG_LABEL:
		this.addOrderNStates = Boolean.parseBoolean(configuration.getValue());
		break;
	    case NameTrainer.GAUSSIAN_PRIOR_VARIANCE_CONFIG_LABEL:
		this.gaussianPriorVariance = Double.parseDouble(configuration.getValue());
		break;
	    case NameTrainer.NUM_ITERATIONS_CONFIG_LABEL:
		this.numIterations = Integer.parseInt(configuration.getValue());
		break;
	    case NameTrainer.NUM_RESETS_CONFIG_LABEL:
		this.numResets = Integer.parseInt(configuration.getValue());
		break;
	    case NameTrainer.NUM_THREADS_CONFIG_LABEL:
		this.numTreads = Integer.parseInt(configuration.getValue());
		break;
	    default:
		throw new IllegalArgumentException("configuration name not known: " + configuration.getName());
	    }
	}
    }

}
