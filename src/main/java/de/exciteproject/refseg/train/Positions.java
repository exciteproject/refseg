package de.exciteproject.refseg.train;

public class Positions {

    private boolean beginning;
    private boolean middle;
    private boolean end;

    public Positions(boolean beginning, boolean middle, boolean end) {
        super();
        this.beginning = beginning;
        this.middle = middle;
        this.end = end;
    }

    public boolean isValid(int position, int lastElementPosition) {
        if ((position < 0) || (lastElementPosition < 0)) {
            throw new IllegalArgumentException("one of the positions is negative");
        }
        if (position > lastElementPosition) {
            throw new IllegalArgumentException("position is grater than lastElementPosition");
        }
        if ((position == 0)) {
            return this.beginning;
        } else {
            if ((position < lastElementPosition)) {
                return this.middle;
            } else {
                if ((position == lastElementPosition)) {
                    return this.end;
                }
            }
        }
        return false;
    }
}
