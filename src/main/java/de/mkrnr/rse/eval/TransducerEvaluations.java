package de.mkrnr.rse.eval;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import de.mkrnr.rse.util.Configuration;
import de.mkrnr.rse.util.JsonHelper;

public class TransducerEvaluations extends Evaluations {
    public void writeStatistics(File outputFile, List<Configuration> crfConfigurations,
            List<Configuration> transducerTrainerConfigurations) {
        LinkedHashMap<String, Object> statisticsMap = new LinkedHashMap<String, Object>();
        statisticsMap.put("aggregated", this.getAggregatedResults());
        statisticsMap.put("crfConfiguration", crfConfigurations);
        statisticsMap.put("transducerTrainerConfiguration", transducerTrainerConfigurations);
        statisticsMap.put("evaluations", this.getEvaluationsMap());

        JsonHelper.writeToFile(statisticsMap, outputFile);
    }

}
