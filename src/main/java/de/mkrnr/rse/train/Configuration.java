package de.mkrnr.rse.train;

public class Configuration {
    private String configurationName;

    private Boolean configurationBooleanValue;
    private Double configurationDoubleValue;

    public Configuration(String configurationName, boolean configurationValue) {
        this.configurationName = configurationName;
        this.configurationBooleanValue = configurationValue;
    }

    public Configuration(String configurationName, double configurationValue) {
        this.configurationName = configurationName;
        this.configurationDoubleValue = configurationValue;
    }

    public boolean getBooleanValue() {
        if (this.configurationBooleanValue == null) {
            // TODO find better exception
            throw new NullPointerException("boolean value not set");
        } else {
            return this.configurationBooleanValue;
        }
    }

    public double getDoubleValue() {
        if (this.configurationDoubleValue == null) {
            // TODO find better exception
            throw new NullPointerException("double value not set");
        } else {
            return this.configurationDoubleValue;
        }
    }

    public String getName() {
        return this.configurationName;
    }

}
