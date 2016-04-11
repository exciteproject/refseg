package de.mkrnr.rse.util;

public class Configuration {
    private String configurationName;
    private String configurationValue;

    public Configuration(String configurationName, String configurationValue) {
        this.configurationName = configurationName;
        this.configurationValue = configurationValue;
    }

    public boolean getBooleanValue() {
        return Boolean.parseBoolean(this.configurationValue);
    }

    public double getDoubleValue() {
        return Double.parseDouble(this.configurationValue);
    }

    public String getName() {
        return this.configurationName;
    }

}
