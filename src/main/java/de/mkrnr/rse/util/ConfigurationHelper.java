package de.mkrnr.rse.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationHelper {

    public static Map<String, String> asMap(List<Configuration> configurations) {
	Map<String, String> configurationMap = new HashMap<String, String>();
	for (Configuration configuration : configurations) {
	    configurationMap.put(configuration.getName(), configuration.getValue());
	}
	return configurationMap;
    }
}
