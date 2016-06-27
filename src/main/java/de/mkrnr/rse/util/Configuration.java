package de.mkrnr.rse.util;

public class Configuration {
    private String configurationName;
    private String configurationValue;

    public Configuration(String configurationName, String configurationValue) {
	this.configurationName = configurationName;
	this.configurationValue = configurationValue;
    }

    public String getName() {
	return this.configurationName;
    }

    public String getValue() {
	return this.configurationValue;
    }

}
