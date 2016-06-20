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

import cc.mallet.fst.CRF;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.fst.ViterbiWriter;
import cc.mallet.fst.semi_supervised.CRFTrainerByGE;
import cc.mallet.fst.semi_supervised.FSTConstraintUtil;
import cc.mallet.fst.semi_supervised.constraints.GEConstraint;
import cc.mallet.fst.semi_supervised.constraints.OneLabelKLGEConstraints;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.util.FileMerger;
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
	features.add("FIRSTNAME");
	features.add("LASTNAME");

	nameTrainer.train(new File(args[0]), new File(args[1]), new File(args[2]), features, new File(args[3]),
		new File(args[4]));
    }

    public void train(File trainingDataInputDirectory, File testingDataInputDirectory, File nameConstraintFile,
	    List<String> features, File firstNameFile, File lastNameFile) throws IOException {
	if (!trainingDataInputDirectory.isDirectory()) {
	    throw new IllegalArgumentException("trainingDataInputDirectory is not a directory");
	}

	List<File> trainingFiles = Arrays.asList(trainingDataInputDirectory.listFiles());

	// TODO change to true
	File tempMergedTrainingFile = TempFileHelper.getTempFile("train", false);

	this.createMergedTrainingFile(trainingFiles, tempMergedTrainingFile);

	List<File> testingFiles = Arrays.asList(testingDataInputDirectory.listFiles());

	// TODO change to true
	File tempMergedTestingFile = TempFileHelper.getTempFile("test", false);

	FileMerger.mergeFiles(testingFiles, tempMergedTestingFile);

	FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(firstNameFile, lastNameFile);

	SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

	SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(features);

	InstanceList trainingInstances = InstanceListBuilder.build(tempMergedTrainingFile, serialPipes);
	InstanceList testingInstances = InstanceListBuilder.build(tempMergedTestingFile, serialPipes);

	serialPipes.getTargetAlphabet().lookupIndex("O");
	serialPipes.setTargetProcessing(true);

	Alphabet targets = serialPipes.getTargetAlphabet();

	StringBuffer buf = new StringBuffer("Labels:");
	for (int i = 0; i < targets.size(); i++) {
	    buf.append(" ").append(targets.lookupObject(i).toString());
	}

	// TODO only use this if no viterbiWriter for training data
	Iterator<Instance> iter = trainingInstances.iterator();
	while (iter.hasNext()) {
	    Instance instance = iter.next();
	    instance.unLock();
	    // instance.setProperty("target", instance.getTarget());
	    instance.setTarget(null);
	    instance.lock();
	}

	// Pattern forbiddenPat = Pattern.compile("\\s");
	// Pattern allowedPat = Pattern.compile(".*");
	CRF crf = new CRF(trainingInstances.getPipe(), (Pipe) null);
	String startName = crf.addOrderNStates(trainingInstances, new int[] { 1 }, null, "O", null, null, true);
	for (int i = 0; i < crf.numStates(); i++) {
	    crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
	}
	crf.getState(startName).setInitialWeight(0.0);
	crf.setWeightsDimensionDensely();

	ArrayList<GEConstraint> constraintsList = this.getKLGEConstraints(nameConstraintFile, trainingInstances);

	CRFTrainerByGE trainer = new CRFTrainerByGE(crf, constraintsList, 1);
	trainer.setGaussianPriorVariance(10.0);
	trainer.setNumResets(4);
	// ViterbiWriter viterbiTrainWriter = new ViterbiWriter("dis_con_crf",
	// // output
	// new InstanceList[] { trainingInstances }, new String[] { "train" }) {
	//
	// @Override
	// public boolean precondition(TransducerTrainer tt) {
	// return (tt.getIteration() % 10) == 0;
	// }
	// };
	// trainer.addEvaluator(viterbiTrainWriter);

	ViterbiWriter viterbiTestWriter = new ViterbiWriter("dis_con_crf", // output
		new InstanceList[] { testingInstances }, new String[] { "test" }) {

	    @Override
	    public boolean precondition(TransducerTrainer tt) {
		return (tt.getIteration() % 10) == 0;
	    }
	};
	trainer.addEvaluator(viterbiTestWriter);

	// add evaluator
	trainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));

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
			if (Math.random() < 0.1) {
			    bufferedWriter.write(word + " " + "I-O" + System.lineSeparator());
			} else {
			    // TODO do this more elegant
			    if (Math.random() > 0.5) {
				if (Math.random() > 0.5) {
				    bufferedWriter.write(word + " " + "B-FN" + System.lineSeparator());
				} else {
				    bufferedWriter.write(word + " " + "I-FN" + System.lineSeparator());
				}
			    } else {
				if (Math.random() > 0.5) {
				    bufferedWriter.write(word + " " + "B-LN" + System.lineSeparator());
				} else {
				    bufferedWriter.write(word + " " + "I-LN" + System.lineSeparator());
				}
			    }
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
