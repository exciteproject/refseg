package de.mkrnr.rse.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.PerClassAccuracyEvaluator;
import cc.mallet.fst.TokenAccuracyEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.fst.ViterbiWriter;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.SimpleTaggerSentence2TokenSequence;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.InstanceList;
import de.mkrnr.rse.pipe.LastNamePipe;
import de.mkrnr.rse.pipe.RegexPipe;

public class TestCRF {

    public static void main(String[] args) throws FileNotFoundException {

        String trainingFilename = args[0];
        String testingFilename = args[1];
        ArrayList<Pipe> pipes = new ArrayList<Pipe>();

        int[][] conjunctions = new int[2][];
        conjunctions[0] = new int[] { -1 };
        conjunctions[1] = new int[] { 1 };

        pipes.add(new SimpleTaggerSentence2TokenSequence());
        pipes.add(new OffsetConjunctions(conjunctions));
        pipes.add(new TokenTextCharSuffix("C1=", 1));
        pipes.add(new TokenTextCharSuffix("C2=", 2));
        pipes.add(new TokenTextCharSuffix("C3=", 3));

        pipes.add(new LastNamePipe("LASTNAME", new File(args[2])));

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
        pipes.add(new TokenSequence2FeatureVectorSequence());

        Pipe pipe = new SerialPipes(pipes);

        InstanceList trainingInstances = new InstanceList(pipe);
        InstanceList testingInstances = new InstanceList(pipe);

        // trainingInstances.addThruPipe(new LineGroupIterator(
        // new BufferedReader(new InputStreamReader(new GZIPInputStream(new
        // FileInputStream(trainingFilename)))),
        // Pattern.compile("^\\s*$"), true));
        trainingInstances.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(trainingFilename))),
                        Pattern.compile("^\\s*$"), true));
        // testingInstances.addThruPipe(new LineGroupIterator(
        // new BufferedReader(new InputStreamReader(new GZIPInputStream(new
        // FileInputStream(testingFilename)))),
        // Pattern.compile("^\\s*$"), true));
        testingInstances.addThruPipe(
                new LineGroupIterator(new BufferedReader(new InputStreamReader(new FileInputStream(testingFilename))),
                        Pattern.compile("^\\s*$"), true));

        CRF crf = new CRF(pipe, null);
        // crf.addStatesForLabelsConnectedAsIn(trainingInstances);
        crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        crf.addStartState();

        CRFTrainerByLabelLikelihood trainer = new CRFTrainerByLabelLikelihood(crf);
        trainer.setGaussianPriorVariance(10.0);

        // CRFTrainerByStochasticGradient trainer = new
        // CRFTrainerByStochasticGradient(crf, 1.0);

        // CRFTrainerByL1LabelLikelihood trainer = new
        // CRFTrainerByL1LabelLikelihood(crf, 0.75);

        trainer.addEvaluator(new PerClassAccuracyEvaluator(trainingInstances, "training"));
        trainer.addEvaluator(new PerClassAccuracyEvaluator(testingInstances, "testing"));
        trainer.addEvaluator(new TokenAccuracyEvaluator(testingInstances, "testing"));

        // *Note*: the output size can grow very big very quickly
        ViterbiWriter viterbiWriter = new ViterbiWriter("output/ner_crf", // output
                // file
                // prefix
                new InstanceList[] { trainingInstances, testingInstances }, new String[] { "train", "test" }) {
            @Override
            public boolean precondition(TransducerTrainer tt) {
                return (tt.getIteration() % Integer.MAX_VALUE) == 45;
            }
        };
        trainer.addEvaluator(viterbiWriter);
        trainer.train(trainingInstances);

    }

}
