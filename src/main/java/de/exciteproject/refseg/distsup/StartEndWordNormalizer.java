package de.exciteproject.refseg.distsup;

public class StartEndWordNormalizer extends WordNormalizer {

    @Override
    public String normalizeWord(String word) {
        return word.replaceFirst("^(\\W)+", "").replaceFirst("(\\W+)$", "");
    }
}
