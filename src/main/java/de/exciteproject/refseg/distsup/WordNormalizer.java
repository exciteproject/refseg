package de.exciteproject.refseg.distsup;

/**
 * Abstract class for normalizing individual words, for example by removing
 * leading or trailing punctuation
 */
public abstract class WordNormalizer {

    public abstract String normalizeWord(String word);
}
