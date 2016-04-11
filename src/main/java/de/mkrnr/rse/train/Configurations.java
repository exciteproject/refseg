package de.mkrnr.rse.train;

import java.util.ArrayList;
import java.util.List;

public class Configurations {

    private List<Configuration> configurations;

    public Configurations() {
        this.configurations = new ArrayList<Configuration>();

    }

    public void addConfiguration(String configurationName, boolean configurationValue) {
        this.configurations.add(new Configuration(configurationName, configurationValue));
    }

    public void addConfiguration(String configurationName, double configurationValue) {
        this.configurations.add(new Configuration(configurationName, configurationValue));
    }

    public List<Configuration> getConfigurations() {
        return this.configurations;
    }

}
