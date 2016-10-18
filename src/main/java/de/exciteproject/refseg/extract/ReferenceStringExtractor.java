package de.exciteproject.refseg.extract;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReferenceStringExtractor {
    private Pattern punctuationPattern;

    public ReferenceStringExtractor() {
        this.punctuationPattern = Pattern.compile("^(\\d+)(\\p{Punct}+)(\\p{Lu}+)(.*)");

    }

    public abstract List<String> extract(File pdfFile);

    protected String normalizeReferenceString(String referenceString) {
        String normalizedString = this.splitReferenceCount(referenceString);
        return normalizedString;
    }

    protected String splitReferenceCount(String referenceString) {

        Matcher m = this.punctuationPattern.matcher(referenceString);

        if (m.find()) {
            return (m.group(1) + m.group(2) + " " + m.group(3) + m.group(4));
        } else {
            return referenceString;
        }
    }

}
