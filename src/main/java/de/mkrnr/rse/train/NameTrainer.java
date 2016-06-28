package de.mkrnr.rse.train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.mkrnr.rse.util.Configuration;
import de.mkrnr.rse.util.ConfigurationHelper;

public class NameTrainer {

    public TransducerTrainer train(InstanceList trainingInstances, InstanceList testingInstances,
	    File nameConstraintFile, List<Configuration> crfConfigurations, List<Configuration> trainerConfigurations,
	    List<TransducerEvaluator> trainingEvaluators, Map<String, String> trainingInformation) throws IOException {
	Map<String, String> crfConfigurationMap = ConfigurationHelper.asMap(crfConfigurations);
	Map<String, String> trainerConfigurationMap = ConfigurationHelper.asMap(trainerConfigurations);

	Pattern forbiddenPat = Pattern.compile("\\s");
	Pattern allowedPat = Pattern.compile(".*");
	CRF crf = new CRF(trainingInstances.getPipe(), (Pipe) null);
	if (crfConfigurationMap.containsKey("addOrderNStates")) {
	    String startName = crf.addOrderNStates(trainingInstances, new int[] { 1 }, null, "O", forbiddenPat,
		    allowedPat, true);
	    for (int i = 0; i < crf.numStates(); i++) {
		crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
	    }
	    crf.getState(startName).setInitialWeight(0.0);

	}
	crf.setWeightsDimensionDensely();

	ArrayList<GEConstraint> constraintsList = this.getKLGEConstraints(nameConstraintFile, trainingInstances);

	CRFTrainerByGE trainer = new CRFTrainerByGE(crf, constraintsList, 1);
	trainer.setGaussianPriorVariance(10.0);
	trainer.setNumResets(4);

	for (TransducerEvaluator trainingEvaluator : trainingEvaluators) {
	    trainer.addEvaluator(trainingEvaluator);
	}

	Instant start = Instant.now();
	// TODO add iterations as parameter
	boolean converged = trainer.train(trainingInstances, 5);
	Instant end = Instant.now();

	trainingInformation.put("timeInMillis", Long.toString(Duration.between(start, end).toMillis()));
	trainingInformation.put("date", LocalDateTime.now().toString());

	trainingInformation.put("iterations", Integer.toString(trainer.getIteration()));
	trainingInformation.put("converged", Boolean.toString(converged));
	// Evaluation evaluation =
	// structuredPerClassAccuracyEvaluator.getEvaluation();
	// evaluation.printEvaluationResults();
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

}
