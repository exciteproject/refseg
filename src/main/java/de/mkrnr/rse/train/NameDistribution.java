package de.mkrnr.rse.train;

public class NameDistribution {

    private int firstNameCount;
    private int lastNameCount;

    public void addToFirstNameCount(int count) {
	this.firstNameCount += count;
    }

    public void addToLastNameCount(int count) {
	this.lastNameCount += count;
    }

    public double getFirstNamePercentage() {
	return ((double) this.firstNameCount) / (this.firstNameCount + this.lastNameCount);
    }

    public double getLastNamePercentage() {
	return ((double) this.lastNameCount) / (this.firstNameCount + this.lastNameCount);
    }
}
