package de.exciteproject.refseg.pipe;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.tsf.OffsetConjunctions;

/**
 * Class for generating feature pipes based on regular expressions.
 * <p>
 * TODO: move firstname and lastname pipes to separate class
 */
public class FeaturePipeProvider {

    private HashMap<String, Pipe> featurePipes;
    private File firstNameFile;
    private File lastNameFile;

    public FeaturePipeProvider() {
        this.createFeaturePipes();
    }

    public FeaturePipeProvider(File firstNameFile, File lastNameFile) {
        this.firstNameFile = firstNameFile;
        this.lastNameFile = lastNameFile;

        this.createFeaturePipes();
    }

    public Pipe getPipe(String featureName) {
        return this.featurePipes.get(featureName);
    }

    public String[] getPipeLabels() {
        return this.featurePipes.keySet().toArray(new String[0]);
    }

    private void addRegexPipe(String featureName, String pattern) {
        this.featurePipes.put(featureName, new RegexPipe(featureName, Pattern.compile(pattern)));
    }

    private void createFeaturePipes() {
        this.featurePipes = new HashMap<String, Pipe>();

        // add featurePipes that use a RegexPipe

        // matches tokens where all letters are lower cased
        this.addRegexPipe("ALLLOWERCASE", "([^\\p{L}]*\\p{javaLowerCase}+[^\\p{L}]*)+");

        // matches tokens where all letters are capitalized
        this.addRegexPipe("ALLCAPS", "([^\\p{L}]*\\p{javaUpperCase}+[^\\p{L}]*)+");

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
        this.addRegexPipe("ENDSWITHCOMMA", ".*,");

        // matches tokens that end with a dash
        this.addRegexPipe("CONTAINSDASH", ".+-.+");

        // matches tokens that end with a dash
        this.addRegexPipe("CONTAINSCOMMA", ".+,.+");

        // matches tokens that end with a dash
        this.addRegexPipe("CONTAINSPERIOD", ".+\\..+");

        // matches tokens that end with a dash
        this.addRegexPipe("ENDSWITHDASH", ".*-");

        // matches tokens that end with a dash
        this.addRegexPipe("ENDSWITHCOLON", ".*:");

        // matches tokens that end with a dash
        this.addRegexPipe("ENDSWITHSEMICOLON", ".*;");

        // matches tokens with exactly one number
        this.addRegexPipe("NUMBER", "\\D*\\d+\\D*");

        // matches tokens with more than one number
        this.addRegexPipe("NUMBERS", ".*\\d+\\D+\\d+.*");

        this.addRegexPipe("BRACKETS", ".*\\[.*\\].*");

        this.addRegexPipe("BRACES", ".*\\(.*\\).*");

        String monthNames = "(?ui)(January|February|March|April|May|June|July|August|September|October|November|December)";
        String monthAbbreviations = "((?ui)(Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[.]?)";

        // matches tokens that contain a month name
        this.addRegexPipe("MONTH", "([^\\p{L}]*|.*[^\\p{L}]+)((" + monthNames + ")|(" + monthAbbreviations
                + "))([^\\p{L}]*|[^\\p{L}]+.*)");

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int decadeNumber = (year / 10) % 10;
        int yearNumber = year % 10;

        // matches tokens that match a year between 1699 and the current year
        // with surrounding non-digit chars
        this.addRegexPipe("YEAR", "\\D*(1[6-9][0-9][0-9]|20[0-" + decadeNumber + "][0-" + yearNumber + "])\\D*");

        // TODO add DATE

        // add firstname and lastname pipes
        if (this.firstNameFile != null) {
            String firstNamePipeLabel = "FIRSTNAME";
            this.featurePipes.put(firstNamePipeLabel, new NamePipe(firstNamePipeLabel, this.firstNameFile));
        }

        if (this.lastNameFile != null) {
            String lastNamePipeLabel = "LASTNAME";
            this.featurePipes.put(lastNamePipeLabel, new NamePipe(lastNamePipeLabel, this.lastNameFile));
        }

        int[][] conjunctions = new int[2][];
        conjunctions[0] = new int[] { -1 };
        conjunctions[1] = new int[] { 1 };
        this.featurePipes.put("CONJUNCTIONS", new OffsetConjunctions(conjunctions));

    }

}
