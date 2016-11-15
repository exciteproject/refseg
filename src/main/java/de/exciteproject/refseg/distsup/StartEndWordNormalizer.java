package de.exciteproject.refseg.distsup;

/**
 * Class for normalizing the beginning and end of individual words
 */
public class StartEndWordNormalizer extends WordNormalizer {

    @Override
    public String normalizeWord(String word) {
        return word.replaceFirst("^(\\W)+", "").replaceFirst("(\\W+)$", "");
    }
}
