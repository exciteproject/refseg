package de.mkrnr.rse.train;

public class NameDistribution {

    public int bFirstNameCount;
    public int iFirstNameCount;
    public int bLastNameCount;
    public int iLastNameCount;
    public int otherCount;

    public int getSum() {
	return this.bFirstNameCount + this.iFirstNameCount + this.bLastNameCount + this.iLastNameCount
		+ this.otherCount;
    }
}
