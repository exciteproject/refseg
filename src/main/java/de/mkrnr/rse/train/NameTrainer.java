package de.mkrnr.rse.train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import cc.mallet.fst.CRF;
import cc.mallet.fst.semi_supervised.FSTConstraintUtil;
import cc.mallet.fst.semi_supervised.constraints.GEConstraint;
import cc.mallet.fst.semi_supervised.constraints.OneLabelKLGEConstraints;
import cc.mallet.fst.semi_supervised.tui.SimpleTaggerWithConstraints;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.util.InstanceListBuilder;
import de.mkrnr.rse.util.TempFileHelper;

public class NameTrainer {
    public static void main(String[] args) throws IOException {
	NameTrainer nameTrainer = new NameTrainer();

	List<String> features = new ArrayList<String>();
	features.add("CAPITALIZED");
	features.add("ONELETTER");
	features.add("ENDSWITHPERIOD");
	features.add("PERIOD");
	features.add("PERIODS");
	features.add("ENDSWITHCOMMA");
	features.add("NUMBER");
	features.add("NUMBERS");
	features.add("BRACKETS");
	features.add("BRACES");
	features.add("MONTH");
	features.add("YEAR");

	nameTrainer.train(new File(args[0]), new File(args[1]), features);
    }

    /**
     * Merges the inputFiles at the end of outputFile.
     *
     * @param inputFiles
     * @param outputFile
     * @return
     * @throws IOException
     */
    public File createMergedTrainingFile(List<File> inputFiles, File outputFile) throws IOException {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

	for (File inputFile : inputFiles) {
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		String[] lineSplit = line.split("\\s+");
		for (String word : lineSplit) {
		    if (!word.isEmpty()) {
			// TODO do this more elegant
			if (Math.random() > 0.5) {
			    bufferedWriter.write(word + " " + "fn" + System.lineSeparator());
			} else {
			    bufferedWriter.write(word + " " + "ln" + System.lineSeparator());
			}
		    }
		}
	    }
	    bufferedReader.close();
	    bufferedWriter.write(System.lineSeparator());
	}
	bufferedWriter.close();
	return outputFile;
    }

    public void train(File trainingDataInputDirectory, File nameConstraintFile, List<String> features)
	    throws IOException {
	if (!trainingDataInputDirectory.isDirectory()) {
	    throw new IllegalArgumentException("trainingDataInputDirectory is not a directory");
	}

	List<File> trainingFiles = Arrays.asList(trainingDataInputDirectory.listFiles());

	// TODO change to true
	File tempMergedTrainingFile = TempFileHelper.getTempFile("train", false);

	System.out.println(tempMergedTrainingFile);

	this.createMergedTrainingFile(trainingFiles, tempMergedTrainingFile);

	// TODO add firstname and lastname files or change method signature
	FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(null, null);

	SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

	SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(features);

	InstanceList inputInstances = InstanceListBuilder.build(tempMergedTrainingFile, serialPipes);

	Iterator<Instance> iter = inputInstances.iterator();
	while (iter.hasNext()) {
	    Instance instance = iter.next();
	    instance.unLock();
	    instance.setProperty("target", instance.getTarget());
	    instance.setTarget(null);
	    instance.lock();
	}

	// BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new
	// File("/tmp/trainingInstances.txt")));
	// bufferedWriter.write(trainingInstances.getDataAlphabet().toString());
	// bufferedWriter.close();

	ArrayList<GEConstraint> klGEConstraints = this.getKLGEConstraints(nameConstraintFile, inputInstances);

	// random split between training and testing
	double trainingFraction = 0.8;
	Random r = new Random(0);
	InstanceList[] trainingLists = inputInstances.split(r, new double[] { trainingFraction, 1 - trainingFraction });
	InstanceList trainingInstances = trainingLists[0];
	InstanceList testingInstances = trainingLists[1];

	// CRF crf = getCRF(trainingInstances, ordersOption.value,
	// defaultOption.value, forbiddenOption.value, allowedOption.value,
	// true);
	CRF crf = SimpleTaggerWithConstraints.getCRF(trainingInstances, new int[] { 1 }, "O", "\\s", ".*", true);

	// crf = trainGE(trainingInstances, testingInstances, klGEConstraints,
	// crf, eval, iterationsOption.value, gaussianVarianceOption.value,
	// numResets.value);

	// TODO fix training
	crf = SimpleTaggerWithConstraints.trainGE(trainingInstances, testingInstances, klGEConstraints, crf, null, 500,
		10.0, 4);
	// System.out.println(trainingInstances.getDataAlphabet().lookupIndex("Rao",
	// false));

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
