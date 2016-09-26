package de.exciteproject.refseg.util;

import com.beust.jcommander.IStringConverter;

public class ConfigurationConverter implements IStringConverter<Configuration> {
    @Override
    public Configuration convert(String value) {
        String[] valueSplit = value.split("=");
        if (valueSplit.length == 2) {
            Configuration configuration = new Configuration(valueSplit[0], valueSplit[1]);
            return configuration;
        } else {
            throw new IllegalArgumentException("parameter should contain an equal sign: " + value);
        }
    }
}
