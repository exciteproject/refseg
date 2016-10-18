package de.exciteproject.refseg.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

public class CermineReferenceStringExtractor extends ReferenceStringExtractor {

    public static void main(String[] args) throws IOException {

        File inputDir = new File(args[0]);
        for (File file : inputDir.listFiles()) {
            CermineReferenceStringExtractor cermineReferenceStringExtractor = new CermineReferenceStringExtractor();

            List<String> references = cermineReferenceStringExtractor.extract(file);

            String outputFileName = args[1] + "/" + FilenameUtils.removeExtension(file.getName()) + ".txt";
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(outputFileName)));
            for (String reference : references) {
                bufferedWriter.write(reference);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }

    }

    @Override
    public List<String> extract(File pdfFile) {
        List<String> references = new ArrayList<String>();
        try {
            ContentExtractor extractor = new ContentExtractor();
            InputStream inputStream;
            inputStream = new FileInputStream(pdfFile);
            extractor.setPDF(inputStream);
            List<BibEntry> result = extractor.getReferences();
            for (BibEntry bibEntry : result) {
                String referenceString = bibEntry.getText();
                String normalizedReferenceString = this.normalizeReferenceString(referenceString);
                references.add(normalizedReferenceString);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AnalysisException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return references;
    }

}
