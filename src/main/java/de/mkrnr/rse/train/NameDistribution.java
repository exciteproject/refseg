package de.mkrnr.rse.train;

public class NameDistribution {

    public double bFirstNameCount;
    public double iFirstNameCount;
    public double bLastNameCount;
    public double iLastNameCount;
    public double otherCount;
    public double iOtherCount;

    public double getSum() {
	return this.bFirstNameCount + this.iFirstNameCount + this.bLastNameCount + this.iLastNameCount
		+ this.otherCount;
    }
}
