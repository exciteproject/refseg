package de.mkrnr.rse.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.mkrnr.rse.util.JsonHelper;

public class Evaluations {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    private List<Evaluation> evaluations;

    public Evaluations() {
        this.evaluations = new ArrayList<Evaluation>();
    }

    public void addEvaluation(Evaluation evaluation) {
        this.evaluations.add(evaluation);
    }

    public void writeAggregatedResults(File outputFile) {
        TreeMap<String, Double> aggregatedResults = new TreeMap<String, Double>();

        for (Evaluation evaluation : this.evaluations) {
            for (Entry<String, Double> result : evaluation.getEvaluationResults().entrySet()) {
                if (aggregatedResults.containsKey(result.getKey())) {
                    aggregatedResults.put(result.getKey(), aggregatedResults.get(result.getKey()) + result.getValue());
                } else {
                    aggregatedResults.put(result.getKey(), result.getValue());
                }
            }
        }

        // String aggregatedString = "";
        for (Entry<String, Double> aggregatedResult : aggregatedResults.entrySet()) {
            // aggregatedString += aggregatedResult.getKey() + ": "
            // + (aggregatedResult.getValue() / this.evaluations.size());
            aggregatedResult.setValue(aggregatedResult.getValue() / this.evaluations.size());

        }

        JsonHelper.writeToFile(aggregatedResults, outputFile);
    }

    public void writeEvaluations(File outputFile) {

        TreeMap<String, TreeMap<String, Double>> aggregatedEvaluations = new TreeMap<String, TreeMap<String, Double>>();
        for (Evaluation evaluation : this.evaluations) {
            aggregatedEvaluations.put(evaluation.getName(), evaluation.getEvaluationResults());
        }
        JsonHelper.writeToFile(aggregatedEvaluations, outputFile);
    }
}
