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
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.SimpleTagger.SimpleTaggerSentence2FeatureVectorSequence;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.semi_supervised.CRFTrainerByGE;
import cc.mallet.fst.semi_supervised.FSTConstraintUtil;
import cc.mallet.fst.semi_supervised.constraints.GEConstraint;
import cc.mallet.fst.semi_supervised.constraints.OneLabelKLGEConstraints;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
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

    public void train(File trainingDataInputDirectory, File nameConstraintFile, List<String> features)
	    throws IOException {
	if (!trainingDataInputDirectory.isDirectory()) {
	    throw new IllegalArgumentException("trainingDataInputDirectory is not a directory");
	}

	List<File> trainingFiles = Arrays.asList(trainingDataInputDirectory.listFiles());

	// TODO change to true
	File tempMergedTrainingFile = TempFileHelper.getTempFile("train", false);

	this.createMergedTrainingFile(trainingFiles, tempMergedTrainingFile);

	FileReader tempMergedTrainingFileReader = new FileReader(tempMergedTrainingFile);

	FileReader nameConstraintFileReader = new FileReader(nameConstraintFile);

	// TODO add firstname and lastname files or change method signature
	// FeaturePipeProvider featurePipeProvider = new
	// FeaturePipeProvider(null, null);
	//
	// SerialPipesBuilder serialPipesBuilder = new
	// SerialPipesBuilder(featurePipeProvider);
	//
	// SerialPipes serialPipes =
	// serialPipesBuilder.createSerialPipes(features);
	//
	//
	// InstanceList inputInstances =
	// InstanceListBuilder.build(tempMergedTrainingFile, serialPipes);

	Pipe p = new SimpleTaggerSentence2FeatureVectorSequence();

	p.getTargetAlphabet().lookupIndex("O");

	p.setTargetProcessing(true);
	InstanceList trainingInstances = new InstanceList(p);
	trainingInstances
		.addThruPipe(new LineGroupIterator(tempMergedTrainingFileReader, Pattern.compile("^\\s*$"), true));

	Alphabet targets = p.getTargetAlphabet();
	StringBuffer buf = new StringBuffer("Labels:");
	for (int i = 0; i < targets.size(); i++) {
	    buf.append(" ").append(targets.lookupObject(i).toString());
	}

	// TODO what does this do? remove target labels? any impact?
	// Iterator<Instance> iter = trainingData.iterator();
	// while (iter.hasNext()) {
	// Instance instance = iter.next();
	// instance.unLock();
	// instance.setProperty("target", instance.getTarget());
	// instance.setTarget(null);
	// instance.lock();
	// }

	// BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new
	// File("/tmp/trainingInstances.txt")));
	// bufferedWriter.write(trainingInstances.getDataAlphabet().toString());
	// bufferedWriter.close();

	Pattern forbiddenPat = Pattern.compile("\\s");
	Pattern allowedPat = Pattern.compile(".*");
	CRF crf = new CRF(trainingInstances.getPipe(), (Pipe) null);
	String startName = crf.addOrderNStates(trainingInstances, new int[] { 1 }, null, "O", forbiddenPat, allowedPat,
		true);
	for (int i = 0; i < crf.numStates(); i++) {
	    crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
	}
	crf.getState(startName).setInitialWeight(0.0);
	crf.setWeightsDimensionDensely();

	ArrayList<GEConstraint> constraintsList = this.getKLGEConstraints(nameConstraintFile, trainingInstances);

	// random split between training and testing
	// double trainingFraction = 0.8;
	// Random r = new Random(0);
	// InstanceList[] trainingLists = trainingData.split(r, new double[] {
	// trainingFraction, 1 - trainingFraction });
	// InstanceList trainingInstances = trainingLists[0];
	// InstanceList testingInstances = trainingLists[1];

	CRFTrainerByGE trainer = new CRFTrainerByGE(crf, constraintsList, 1);
	trainer.setGaussianPriorVariance(10.0);
	trainer.setNumResets(4);
	trainer.train(trainingInstances, 500);

    }

    /**
     * Merges the inputFiles at the end of outputFile.
     *
     * @param inputFiles
     * @param outputFile
     * @return
     * @throws IOException
     */
    private File createMergedTrainingFile(List<File> inputFiles, File outputFile) throws IOException {
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
