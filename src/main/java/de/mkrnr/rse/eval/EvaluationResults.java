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
	evaluationResults.writeAsJson(new File("/home/martin/eval-test.json"));

	EvaluationResults readEvaluationsFile = new EvaluationResults();
	readEvaluationsFile.readFromJson(new File("/home/martin/eval-test.json"));
	System.out.println(readEvaluationsFile.evaluations.size());
	System.out.println(readEvaluationsFile.converged);
    }

    private Map<String, Object> configurations;
    private Boolean converged;
    private LocalDateTime endOfExecution;
    private List<Evaluation> evaluations;
    private Long timeInMillis;
    private int iterations;

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

    public boolean getConverged() {
	return this.converged;
    }

    public LocalDateTime getEndOfExecution() {
	return this.endOfExecution;
    }

    public List<Evaluation> getEvaluations() {
	return this.evaluations;
    }

    public int getIterations() {
	return this.iterations;
    }

    public long getTimeInMillis() {
	return this.timeInMillis;
    }

    public void readFromJson(File inputFile) {
	EvaluationResults readEvaluations = (EvaluationResults) JsonHelper.readFromFile(this.getClass(), inputFile);
	this.configurations = readEvaluations.configurations;
	this.converged = readEvaluations.converged;
	this.endOfExecution = readEvaluations.endOfExecution;
	this.evaluations = readEvaluations.evaluations;
	this.timeInMillis = readEvaluations.timeInMillis;
    }

    public void setConverged(boolean converged) {
	this.converged = converged;
    }

    public void setEndOfExecution(LocalDateTime endOfExecution) {
	this.endOfExecution = endOfExecution;
    }

    public void setIterations(int iterations) {
	this.iterations = iterations;
    }

    public void setTimeInMillis(long timeInMillis) {
	this.timeInMillis = timeInMillis;
    }

    public void writeAsJson(File outputFile) {
	JsonHelper.writeToFile(this, outputFile);
    }
}
