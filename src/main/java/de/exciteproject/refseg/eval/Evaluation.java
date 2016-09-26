package de.exciteproject.refseg.eval;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Evaluation implements Serializable {

    private static final long serialVersionUID = -1428096929001039702L;

    private Map<String, Map<String, Object>> results;
    private int iteration;
    private String description;

    public Evaluation() {
	this.results = new TreeMap<String, Map<String, Object>>();
    }

    public void addEvaluationResult(String evaluatedElement, String metric, Object result) {
	if (!this.results.containsKey(evaluatedElement)) {
	    this.results.put(evaluatedElement, new TreeMap<String, Object>());
	}
	this.results.get(evaluatedElement).put(metric, result);
    }

    public String getDescription() {
	return this.description;
    }

    public Map<String, Map<String, Object>> getEvaluationResults() {
	return this.results;
    }

    public int getIteration() {
	return this.iteration;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void setIteration(int iteration) {
	this.iteration = iteration;
    }

    @Override
    public String toString() {
	String string = "";
	string += "Evaluation: " + this.description + System.lineSeparator();
	string += "Iteration: " + this.iteration + System.lineSeparator();

	int doublePrecision = 6;
	for (Entry<String, Map<String, Object>> resultEntry : this.results.entrySet()) {
	    String resultLine = resultEntry.getKey();
	    resultLine += "\t";
	    for (Entry<String, Object> resultValue : resultEntry.getValue().entrySet()) {
		resultLine += resultValue.getKey() + ": ";

		if (resultValue.getValue() instanceof Integer) {
		    int value = (Integer) resultValue.getValue();
		    resultLine += value;

		    // Fill the optical gap

		    int missingSpaces = (doublePrecision + 2) - String.valueOf(value).length();
		    while (missingSpaces > 0) {
			resultLine += " ";
			missingSpaces -= 1;
		    }
		} else {
		    if (resultValue.getValue() instanceof Double) {
			double roundedValue = new BigDecimal((Double) resultValue.getValue())
				.setScale(doublePrecision, BigDecimal.ROUND_HALF_UP).doubleValue();
			resultLine += String.format("%." + doublePrecision + "f", roundedValue);
		    } else {
			throw new ClassCastException("resultValue is neither integer no double");
		    }
		}
		resultLine += "  ";
	    }
	    resultLine = resultLine.replaceFirst("\\s*$", "");
	    string += resultLine + System.lineSeparator();
	}
	return string;

    }
}
