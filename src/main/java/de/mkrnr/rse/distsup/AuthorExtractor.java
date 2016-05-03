package de.mkrnr.rse.distsup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class AuthorExtractor {

    protected void addNamesToMap(String names, HashMap<String, Integer> map) {
	if (map.containsKey(names)) {
	    map.put(names, map.get(names) + 1);
	} else {
	    map.put(names, 1);
	}

    }

    protected void writeMapToFile(HashMap<String, Integer> map, File outputFile) {
	try {
	    BufferedWriter nameWriter = new BufferedWriter(new FileWriter(outputFile));

	    for (Entry<String, Integer> entry : map.entrySet()) {
		System.out.println(entry.getKey());
		nameWriter.write(entry.getKey() + "\t" + entry.getValue() + System.lineSeparator());
	    }

	    nameWriter.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
}
