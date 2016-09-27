package de.exciteproject.refseg.util;

public class Configuration {
    private String name;
    private String value;

    public Configuration(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

}
