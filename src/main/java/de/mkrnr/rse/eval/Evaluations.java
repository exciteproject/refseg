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
        TreeMap<String, List<Double>> aggregatedResults = new TreeMap<String, List<Double>>();

        for (Evaluation evaluation : this.evaluations) {
            for (Entry<String, Double> result : evaluation.getEvaluationResults().entrySet()) {
                if (aggregatedResults.containsKey(result.getKey())) {
                    aggregatedResults.get(result.getKey()).add(result.getValue());
                } else {
                    List<Double> resultList = new ArrayList<Double>();
                    resultList.add(result.getValue());
                    aggregatedResults.put(result.getKey(), resultList);
                }
            }
        }

        TreeMap<String, Double> resultMeans = new TreeMap<String, Double>();

        for (Entry<String, List<Double>> aggregatedResult : aggregatedResults.entrySet()) {
            Double resultSum = 0.0;
            for (Double result : aggregatedResult.getValue()) {
                resultSum += result;
            }
            Double mean = resultSum / aggregatedResult.getValue().size();

            if (!mean.isNaN()) {
                resultMeans.put(aggregatedResult.getKey(), mean);
            }
        }

        JsonHelper.writeToFile(resultMeans, outputFile);
    }

    public void writeEvaluations(File outputFile) {

        TreeMap<String, TreeMap<String, Double>> aggregatedEvaluations = new TreeMap<String, TreeMap<String, Double>>();
        for (Evaluation evaluation : this.evaluations) {
            aggregatedEvaluations.put(evaluation.getName(), evaluation.getEvaluationResults());
        }
        JsonHelper.writeToFile(aggregatedEvaluations, outputFile);
    }
}
