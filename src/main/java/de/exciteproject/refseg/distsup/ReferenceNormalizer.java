package de.exciteproject.refseg.distsup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for normalizing reference strings. Tasks include:
 * <p>
 * * Split words that are separated with punctuation (e.g. Holmes,S.E.,)
 * </p>
 *
 *
 */
public class ReferenceNormalizer {

    private static final Pattern PUNCTUATION_SPLIT_PATTERN;
    static {
        String punctuationSplitPatternString = "(,|\\.|;)(?=[\\w])";
        PUNCTUATION_SPLIT_PATTERN = Pattern.compile(punctuationSplitPatternString);
    }

    public static void main(String[] args) {
        String test = "6. Gromiha,M.M., Thangakani,A.M. and Selvaraj,S. (2006) FOLD-RATE: prediction of protein folding rates from amino acid sequence. Nucleic Acids Res., 34, W70-W74.";
        System.out.println(test);
        System.out.println(ReferenceNormalizer.splitAfterPunctuation(test));

    }

    public static String splitAfterPunctuation(String reference) {
        Matcher matcher = PUNCTUATION_SPLIT_PATTERN.matcher(reference);

        return matcher.replaceAll("$1 ");
    }

}
