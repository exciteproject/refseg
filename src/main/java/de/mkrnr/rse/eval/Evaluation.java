package de.mkrnr.rse.eval;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Evaluation implements Serializable {

    private static final long serialVersionUID = -1428096929001039702L;

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        System.out.println(System.currentTimeMillis());
        System.out.println(System.nanoTime());
    }

    private TreeMap<String, Double> evaluationResults;
    private String name;

    public Evaluation() {
        this("");
    }

    public Evaluation(String name) {
        this.name = name;
        this.evaluationResults = new TreeMap<String, Double>();
    }

    public void addEvaluationResult(String resultName, double result) {
        this.evaluationResults.put(resultName, result);
    }

    public TreeMap<String, Double> getEvaluationResults() {
        return this.evaluationResults;
    }

    public String getName() {
        return this.name;
    }

    public void printEvaluationResults() {
        System.out.println("Evaluation: " + this.name);
        for (Entry<String, Double> resultEntry : this.evaluationResults.entrySet()) {
            System.out.println(resultEntry.getKey() + " " + resultEntry.getValue());
        }
    }

    public void setName(String name) {
        this.name = name;
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
