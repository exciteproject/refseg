package de.mkrnr.rse.eval;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Evaluation implements Serializable {

    private static final long serialVersionUID = -1428096929001039702L;

    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }

    private Map<String, Map<String, Double>> evaluationResults;
    private int iteration;

    public Evaluation() {
	this("");
    }

    public Evaluation(String name) {
	this.evaluationResults = new TreeMap<String, Map<String, Double>>();
    }

    public void addEvaluationResult(String evaluatedElement, String metric, double result) {
	if (!this.evaluationResults.containsKey(evaluatedElement)) {
	    this.evaluationResults.put(evaluatedElement, new TreeMap<String, Double>());
	}
	this.evaluationResults.get(evaluatedElement).put(metric, result);
    }

    public Map<String, Map<String, Double>> getEvaluationResults() {
	return this.evaluationResults;
    }

    public int getIteration() {
	return this.iteration;
    }

    public void printEvaluationResults() {
	System.out.println("Evaluation: ");
	System.out.println("Iteration: " + this.getIteration());

	for (Entry<String, Map<String, Double>> resultEntry : this.evaluationResults.entrySet()) {
	    System.out.print(resultEntry.getKey());
	    for (Entry<String, Double> resultValue : resultEntry.getValue().entrySet()) {
		int doublePrecision = 5;
		Double roundedValue = new BigDecimal(resultValue.getValue())
			.setScale(doublePrecision, BigDecimal.ROUND_HALF_UP).doubleValue();
		System.out.print("\t");
		System.out.print(resultValue.getKey() + ": ");
		System.out.printf("%." + doublePrecision + "f", roundedValue);
	    }
	    System.out.println();
	}
    }

    public void setIteration(int iteration) {
	this.iteration = iteration;
    }

    // @Override
    // public String toString() {
    // String evaluationString = "Evaluation: " + this.name + "\n";
    // for (Entry<String, Double> evaluationResult :
    // this.evaluationResults.entrySet()) {
    // evaluationString += "\t" + evaluationResult.getKey() + ": " +
    // evaluationResult.getValue() + "\n";
    // }

    // return evaluationString;
    // }

}
