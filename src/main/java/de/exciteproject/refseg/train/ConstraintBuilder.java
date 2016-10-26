package de.exciteproject.refseg.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.mkrnr.goddag.Goddag;

public class ConstraintBuilder {

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
        Gson gson = gsonBuilder.create();

        // TODO Set via parameters?
        ConstraintBuilder constraintBuilder = new ConstraintBuilder("B-", "I-");
        constraintBuilder.addNamesCounter("AU", "FN");
        constraintBuilder.addNamesCounter("AU", "LN");

        constraintBuilder.addStringCounter("TI");
        constraintBuilder.addStringCounter("PL");
        constraintBuilder.addStringCounter("PN");
        constraintBuilder.addStringCounter("SO");

        File inputGoddagDirectory = new File(args[0]);
        File outputDistributionFile = new File(args[1]);
        for (File goddagFile : inputGoddagDirectory.listFiles()) {
            Goddag[] goddags = gson.fromJson(new FileReader(goddagFile), Goddag[].class);
            for (Goddag goddag : goddags) {
                constraintBuilder.count(goddag);
            }
        }
        constraintBuilder.writeDistributions(outputDistributionFile);
    }

    private List<Counter> counters;

    private ConstraintCounts constraintCounts;

    private Map<String, Positions> allPositions;

    private List<String> positionNameLabels;

    public ConstraintBuilder(String beginningPrefix, String intermediatePrefix) {
        this();
        Positions beginningPositions = new Positions(true, false, false);
        this.allPositions.put(beginningPrefix, beginningPositions);

        Positions intermediatePositions = new Positions(false, true, true);
        this.allPositions.put(intermediatePrefix, intermediatePositions);

    }

    public ConstraintBuilder(String beginningPrefix, String intermediatePrefix, String endPrefix) {
        this();
        Positions beginningPositions = new Positions(true, false, false);
        this.allPositions.put(beginningPrefix, beginningPositions);

        Positions intermediatePositions = new Positions(false, true, false);
        this.allPositions.put(intermediatePrefix, intermediatePositions);
        Positions endPositions = new Positions(false, false, true);
        this.allPositions.put(endPrefix, endPositions);

    }

    private ConstraintBuilder() {
        this.counters = new ArrayList<Counter>();
        this.constraintCounts = new ConstraintCounts();
        this.allPositions = new LinkedHashMap<String, Positions>();
        this.positionNameLabels = new ArrayList<String>();

    }

    public void writeDistributions(File outputFile) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (String word : this.constraintCounts.getWords()) {
            Map<String, Integer> wordCounts = this.constraintCounts.getWordCounts(word);
            double totalWordCounts = this.constraintCounts.getTotalCounts(word);
            bufferedWriter.write(word);
            for (String positionNameLabel : this.positionNameLabels) {
                bufferedWriter.write("\t" + positionNameLabel + ":"
                        + (wordCounts.getOrDefault(positionNameLabel, 0) / totalWordCounts));
            }
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private void addNamesCounter(String authorLabel, String nameLabel) {
        String[] labelHierarchy = { authorLabel, nameLabel };
        for (Entry<String, Positions> positionsEntry : this.allPositions.entrySet()) {
            String positionNameLabel = positionsEntry.getKey() + nameLabel;
            Counter counter = new ParentPositionCounter(labelHierarchy, positionsEntry.getValue(), positionNameLabel);
            this.counters.add(counter);
            this.positionNameLabels.add(positionNameLabel);
        }
    }

    private void addStringCounter(String stringLabel) {
        String[] labelHierarchy = { stringLabel };
        for (Entry<String, Positions> positionsEntry : this.allPositions.entrySet()) {
            String positionNameLabel = positionsEntry.getKey() + stringLabel;

            Counter counter = new DirectPositionCounter(labelHierarchy, positionsEntry.getValue(), positionNameLabel);
            this.counters.add(counter);
            this.positionNameLabels.add(positionNameLabel);
        }

    }

    private void count(Goddag goddag) {
        for (Counter counter : this.counters) {
            counter.count(goddag, this.constraintCounts);
        }

    }
}
