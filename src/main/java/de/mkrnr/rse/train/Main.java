package de.mkrnr.rse.train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.pipe.FeaturePipeProvider;
import de.mkrnr.rse.pipe.SerialPipesBuilder;
import de.mkrnr.rse.util.Configuration;
import de.mkrnr.rse.util.ConfigurationConverter;
import de.mkrnr.rse.util.InstanceListBuilder;

public class Main {

    public static void main(String[] args) throws IOException {
	Main main = new Main();

	JCommander jCommander;
	try {
	    jCommander = new JCommander(main, args);
	} catch (ParameterException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	    return;
	}

	if (main.help) {
	    jCommander.usage();
	} else {
	    main.run();
	}
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters")
    private boolean help;

    @Parameter(names = { "-log-eval",
	    "--log-eval-during-training" }, description = "logs information about precision, recall, and f1 scores during training")
    private boolean evaluateDuringTraining = false;

    @Parameter(names = { "-test",
	    "--testing-file" }, description = "file that contains per line: word <space> label", required = true, converter = FileConverter.class)
    private File testingFile;

    @Parameter(names = { "-train",
	    "--training-file" }, description = "file that contains per line: word <space> label", required = true, converter = FileConverter.class)
    private File trainingFile;

    @Parameter(names = { "-constraints",
	    "--constraints-file" }, description = "file that contains per line: word <space> label", required = true, converter = FileConverter.class)
    private File constraintsFile;

    @Parameter(names = { "-feat",
	    "--features" }, description = "comma separated list of features", variableArity = true, required = true)
    private List<String> features;

    @Parameter(names = { "-first-names",
	    "--first-names-file" }, description = "file containing first names and counts, separated by tab", required = true, converter = FileConverter.class)
    private File firstNameFile;

    @Parameter(names = { "-last-names",
	    "--last-names-file" }, description = "file containing last names and counts, separated by tab", required = true, converter = FileConverter.class)
    private File lastNameFile;

    @Parameter(names = { "-crf",
	    "--crf-configurations" }, description = "list of key=value for configuring the CRF building", variableArity = true, required = true, converter = ConfigurationConverter.class)
    private List<Configuration> crfConfigurations;

    @Parameter(names = { "-trainer",
	    "--trainer-configurations" }, description = "list of key=valueforconfiguring the trainer building", variableArity = true, converter = ConfigurationConverter.class)
    private List<Configuration> trainerConfigurations = new ArrayList<Configuration>();

    @Parameter(names = { "-other",
	    "--other-label" }, description = "label that is used to specitfy other instances in the training set")
    private String otherLabel = "other";

    private void run() throws IOException {

	FeaturePipeProvider featurePipeProvider = new FeaturePipeProvider(this.firstNameFile, this.lastNameFile);

	SerialPipesBuilder serialPipesBuilder = new SerialPipesBuilder(featurePipeProvider);

	SerialPipes serialPipes = serialPipesBuilder.createSerialPipes(this.features);

	InstanceList trainingInstances = InstanceListBuilder.build(this.trainingFile, serialPipes);
	InstanceList testingInstances = InstanceListBuilder.build(this.testingFile, serialPipes);

	serialPipes.setTargetProcessing(false);

	// TODO only use this if no viterbiWriter for training data
	Iterator<Instance> iter = trainingInstances.iterator();
	while (iter.hasNext()) {
	    Instance instance = iter.next();
	    instance.unLock();
	    // instance.setProperty("target", instance.getTarget());
	    instance.setTarget(null);
	    instance.lock();
	}

	NameTrainer nameTrainer = new NameTrainer();
	nameTrainer.train(trainingInstances, testingInstances, this.constraintsFile, this.features, this.firstNameFile,
		this.lastNameFile, this.crfConfigurations, this.trainerConfigurations);
    }
}
