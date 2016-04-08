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

    private Fold fold;
    private TreeMap<String, Double> evaluationResults;

    public Evaluation() {
    }

    public Evaluation(Fold fold) {
        this.fold = fold;
    }

    public void addEvaluationResult(String resultName, double result) {
        this.evaluationResults.put(resultName, result);
    }

    public void printEvaluationResults() {
        System.out.println("Fold name: " + this.fold.getName());
        for (Entry<String, Double> resultEntry : this.evaluationResults.entrySet()) {
            System.out.println(resultEntry.getKey() + " " + resultEntry.getValue());
        }
    }

    public void setFold(Fold fold) {
        this.fold = fold;
    }

}
