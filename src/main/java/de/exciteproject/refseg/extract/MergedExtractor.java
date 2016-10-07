package de.exciteproject.refseg.extract;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MergedExtractor {

    public static void main(String[] args) throws IOException {
        File tdbDirectory = new File(args[0]);
        File sowiportInputFile = new File(args[1]);
        File outputDirectory = new File(args[2]);
        int maxNumberOfNames = Integer.parseInt(args[3]);
        boolean onlyDifferentiated = Boolean.parseBoolean(args[4]);

        GNDAuthorExtractor gndAuthorExtractor = new GNDAuthorExtractor();
        List<String> nameStringList = gndAuthorExtractor.extractAuthorNames(tdbDirectory, onlyDifferentiated);

        SowiportAuthorExtractor sowiportAuthorExtractor = new SowiportAuthorExtractor();
        nameStringList.addAll(sowiportAuthorExtractor.extractAuthorNames(sowiportInputFile));

        sowiportAuthorExtractor.addNameStringListToMaps(nameStringList, maxNumberOfNames);
        sowiportAuthorExtractor.writeMaps(outputDirectory);
    }
}
