package de.mkrnr.rse.pipe;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;

public class FeaturePipeProvider {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }

    private HashMap<String, Pipe> featurePipes;

    public FeaturePipeProvider(File firstNameFile, File lastNameFile) {
        // String firstNamePipeLabel = "FIRSTNAME";
        // this.featurePipes.put(firstNamePipeLabel, new
        // NamePipe(firstNamePipeLabel, firstNameFile));

        // String lastNamePipeLabel = "LASTNAME";
        // this.featurePipes.put(lastNamePipeLabel, new
        // NamePipe(lastNamePipeLabel, lastNameFile));

        this.createFeaturePipes();
    }

    private void addRegexPipe(String featureName, String pattern) {
        this.featurePipes.put(featureName, new RegexPipe(featureName, Pattern.compile(pattern)));
    }

    private void createFeaturePipes() {
        this.featurePipes = new HashMap<String, Pipe>();

        // add featurePipes that use a RegexPipe

        // matches tokens where the first letter is capitalized
        this.addRegexPipe("CAPITALIZED", "[^\\p{L}]*\\p{Lu}.*");

        // matches tokens that contain exactly one letter
        this.addRegexPipe("ONELETTER", "[^\\p{L}]*\\p{L}[^\\p{L}]*");

        // matches tokens that end with a period
        this.addRegexPipe("ENDSWITHPERIOD", ".*\\.");

        // matches tokens that contain exactly one period
        this.addRegexPipe("PERIOD", "[^\\.]*\\.[^\\.]*");

        // matches tokens that contain multiple periods
        this.addRegexPipe("PERIODS", ".*\\..*\\..*");

        // matches tokens that end with a comma
        this.addRegexPipe("ENDSWITHCOMMA", ".*\\,");

        // matches tokens with exactly one number
        this.addRegexPipe("NUMBER", "\\D*\\d+\\D*");

        // matches tokens with more than one number
        this.addRegexPipe("NUMBERS", ".*\\d+\\D+\\d+.*");

        this.addRegexPipe("BRACKETS", "\\[.*\\]");

        String monthNames = "(?ui)(January|February|March|April|May|June|July|August|September|October|November|December)";
        String monthAbbreviations = "((?ui)(Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[.]?)";

        // matches tokens that contain a month name
        this.addRegexPipe("MONTH", ".*((" + monthNames + ")|(" + monthAbbreviations + ")).*");

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int decadeNumber = (year / 10) % 10;
        int yearNumber = year % 10;

        // matches tokens that match a year between 1599 and the current year
        // with surrounding non-digit chars
        this.addRegexPipe("YEAR",
                "[^\\p{IsDigit}]*(1[5-9][0-9][0-9]|20[0-" + decadeNumber + "][0-" + yearNumber + "])[^\\p{IsDigit}]*");

    }

    public Pipe getPipe(String featureName) {
        return this.featurePipes.get(featureName);
    }

    public String[] getPipeLabels() {
        return this.featurePipes.keySet().toArray(new String[0]);
    }

}
