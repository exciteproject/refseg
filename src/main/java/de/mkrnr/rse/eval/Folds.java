package de.mkrnr.rse.eval;

import java.util.LinkedList;
import java.util.List;

public class Folds {

    private List<Fold> folds;

    public Folds() {
        this.folds = new LinkedList<Fold>();
    }

    public void add(Fold fold) {
        this.folds.add(fold);
    }

    public List<Fold> asList() {
        return this.folds;
    }

}
