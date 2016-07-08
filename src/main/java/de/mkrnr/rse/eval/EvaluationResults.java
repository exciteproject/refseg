package de.mkrnr.rse.eval;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mkrnr.rse.util.JsonHelper;

public class EvaluationResults {

    public static void main(String[] args) {
	EvaluationResults evaluationResults = new EvaluationResults();
	Evaluation evaluation = new Evaluation();
	evaluation.addEvaluationResult("test", "test", 10.0);
	evaluationResults.addEvaluation(evaluation);
	evaluationResults.setConverged(true);
	evaluationResults.setTimeInMillis(42);
	EvaluationResults.writeAsJson(evaluationResults, new File("/home/martin/eval-test.json"));

	EvaluationResults readEvaluationsFile = EvaluationResults.readFromJson(new File("/home/martin/eval-test.json"));
	System.out.println(readEvaluationsFile.evaluations.size());
	System.out.println(readEvaluationsFile.converged);
    }

    public static EvaluationResults readFromJson(File inputFile) {
	return (EvaluationResults) JsonHelper.readFromFile(EvaluationResults.class, inputFile);
    }

    public static void writeAsJson(EvaluationResults evaluationResults, File outputFile) {
	JsonHelper.writeToFile(evaluationResults, outputFile);
    }

    private Map<String, Object> configurations;
    private File constraintsFile;
    private Boolean converged;
    private LocalDateTime endOfExecution;
    private List<Evaluation> evaluations;
    private List<String> features;
    private Integer iterations;
    private LocalDateTime localDateTime;
    private File testingFile;
    private Long timeInMillis;
    private File trainingFile;
    private long memoryInBytes;

    public EvaluationResults() {
	this.evaluations = new ArrayList<Evaluation>();
	this.configurations = new HashMap<String, Object>();
    }

    public void addConfiguration(String name, Object value) {
	this.configurations.put(name, value);
    }

    public void addEvaluation(Evaluation evaluation) {
	this.evaluations.add(evaluation);
    }

    public Map<String, Object> getConfigurations() {
	return this.configurations;
    }

    public File getConstaintsFile() {
	return this.constraintsFile;
    }

    public boolean getConverged() {
	return this.converged;
    }

    public LocalDateTime getEndOfExecution() {
	return this.endOfExecution;
    }

    public List<Evaluation> getEvaluations() {
	return this.evaluations;
    }

    public List<String> getFeatures() {
	return this.features;
    }

    public int getIterations() {
	return this.iterations;
    }

    public LocalDateTime getLocalDateTime() {
	return this.localDateTime;
    }

    public long getMemoryInBytes() {
	return this.memoryInBytes;
    }

    public File getTestingFile() {
	return this.testingFile;
    }

    public long getTimeInMillis() {
	return this.timeInMillis;
    }

    public File getTrainingFile() {
	return this.trainingFile;
    }

    public void setConstraintsFile(File constraintsFile) {
	this.constraintsFile = constraintsFile;
    }

    public void setConverged(boolean converged) {
	this.converged = converged;
    }

    public void setEndOfExecution(LocalDateTime endOfExecution) {
	this.endOfExecution = endOfExecution;
    }

    public void setFeatures(List<String> features) {
	this.features = features;
    }

    public void setIterations(int iterations) {
	this.iterations = iterations;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
	this.localDateTime = localDateTime;
    }

    public void setMemoryInBytes(long memoryInBytes) {
	this.memoryInBytes = memoryInBytes;

    }

    public void setTestingFile(File testingFile) {
	this.testingFile = testingFile;
    }

    public void setTimeInMillis(long timeInMillis) {
	this.timeInMillis = timeInMillis;
    }

    public void setTrainingFile(File trainingFile) {
	this.trainingFile = trainingFile;
    }
}
