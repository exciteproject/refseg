package de.exciteproject.refseg.train;

import de.mkrnr.goddag.Goddag;

public abstract class Counter {

    protected String[] tagLabelHieararchy;
    protected Positions positions;
    protected String constraintCountLabel;

    public Counter(String[] tagLabelHieararchy, Positions positions, String constraintCountLabel) {
        this.tagLabelHieararchy = tagLabelHieararchy;
        this.positions = positions;
        this.constraintCountLabel = constraintCountLabel;
    }

    public abstract void count(Goddag goddag, ConstraintCounts constraintCounts);

}
