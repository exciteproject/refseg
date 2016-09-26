package de.exciteproject.refseg.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;

public class TestPipe {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ArrayList<Pipe> pipes = new ArrayList<Pipe>();

        // Read data from File objects
        pipes.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        // This pattern includes Unicode letters, Unicode numbers,
        // and the underscore character. Alternatives:
        // "\\S+" (anything not whitespace)
        // "\\w+" ( A-Z, a-z, 0-9, _ )
        // "[\\p{L}\\p{N}_]+|[\\p{P}]+" (a group of only letters and numbers OR
        // a group of only punctuation marks)
        // Pattern tokenPattern = Pattern.compile("\\S+");

        // Pattern tokenPattern =
        // Pattern.compile("(\\n)|((\\S)*[[\\p{L}&&\\D]+\\s[\\p{L}&&\\D]+]*\\S*)");

        // Pattern tokenPattern =
        // Pattern.compile("(\\n)|(\\S*[\\p{Alpha}+\\s\\p{Alpha}+]*\\S*)");

        // Tokenize raw strings
        // pipes.add(new CharSequence2TokenSequence(tokenPattern));

        pipes.add(new SimpleTaggerSentence2TokenSequence());
        // pipes.add(new FeaturesInWindow("PREV-", -1, 1));
        // pipes.add(new FeaturesInWindow("NEXT-", 1, 2));

        pipes.add(new NamePipe("LASTNAME", new File(args[1])));

        // works
        pipes.add(new RegexPipe("CAPITALIZED", Pattern.compile("[^\\p{L}]*\\p{Lu}.*")));
        pipes.add(new RegexPipe("ONELETTER", Pattern.compile("[^\\p{L}]*\\p{L}[^\\p{L}]*")));
        pipes.add(new RegexPipe("ENDSWITHPERIOD", Pattern.compile(".*\\.")));
        pipes.add(new RegexPipe("ENDSWITHCOMMA", Pattern.compile(".*\\,")));

        // matches exactly one number in a token
        pipes.add(new RegexPipe("NUMBER", Pattern.compile("\\D*\\d+\\D*")));

        // matches more than one number in a token
        pipes.add(new RegexPipe("NUMBERS", Pattern.compile(".*\\d+\\D+\\d+.*")));
        pipes.add(new RegexPipe("BRACKETS", Pattern.compile("\\[.*\\]")));

        // String monthNames =
        // "([J|j]anuary|[F|f]ebruary|[M|m]arch|[A|a]pril|[M|m]ay|[J|j]une|[J|j]uly|[A|a]ugust|[S|s]eptember|[O|o]ctober|[N|n]ovember|[D|d]ecember)";
        String monthNames = "(?ui)(January|February|March|April|May|June|July|August|September|October|November|December)";
        String monthAbbreviations = "((?ui)(Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[.]?)";
        // String monthNames =
        // "([J|j]anuary|[F|f]ebruary|[M|m]arch|[A|a]pril|[M|m]ay|[J|j]une|[J|j]uly|[A|a]ugust|[S|s]eptember|[O|o]ctober|[N|n]ovember|[D|d]ecember)";
        // String monthAbbreviations =
        // "(([J|j]an|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|" + "Oct|Nov|Dec)[.]?)";

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int decadeNumber = (year / 10) % 10;
        int yearNumber = year % 10;

        pipes.add(new RegexPipe("MONTH", Pattern.compile(".*((" + monthNames + ")|(" + monthAbbreviations + ")).*")));

        // matches any year between 1599 and the current year with surrounding
        // non-digit chars
        pipes.add(new RegexPipe("YEAR", Pattern.compile("[^\\p{IsDigit}]*(1[5-9][0-9][0-9]|20[0-" + decadeNumber
                + "][0-" + yearNumber + "])[^\\p{IsDigit}]*")));

        // pipes.add(new PrintTokenSequenceFeatures());
        pipes.add(new PrintInputAndTarget());

        pipes.add(new TokenSequence2FeatureVectorSequence());

        // PrintWriter out = new PrintWriter(System.out);
        // pipes.add(new SequencePrintingPipe(out));

        Pipe pipe = new SerialPipes(pipes);

        InstanceList trainingInstances = new InstanceList(pipe);

        trainingInstances.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))),
                        // new BufferedReader(new InputStreamReader(new
                        // GZIPInputStream(new FileInputStream(args[0])))),
                        Pattern.compile("^\\s*$"), true));

        // out.close();

    }

}
