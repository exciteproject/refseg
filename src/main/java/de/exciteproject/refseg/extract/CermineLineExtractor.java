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

import de.exciteproject.refseg.util.TextUtils;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.structure.model.BxZone;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

/**
 * Class for extracting individual lines from a PDF file in the correct order
 * using the zoning approach of CERMINE.
 *
 */
public class CermineLineExtractor {

    public static void main(String[] args) throws IOException, AnalysisException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (File file : inputDir.listFiles()) {
            System.out.println(file.getAbsolutePath());
            CermineLineExtractor cermineReferenceStringExtractor = new CermineLineExtractor();

            List<String> references = cermineReferenceStringExtractor.extract(file);

            String outputFileName = outputDir.getAbsolutePath() + "/" + FilenameUtils.removeExtension(file.getName())
                    + ".txt";
            File outputFile = new File(outputFileName);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (String reference : references) {
                bufferedWriter.write(reference);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
    }

    private ContentExtractor extractor;

    public CermineLineExtractor() throws AnalysisException {
        this.extractor = new ContentExtractor();
    }

    public List<String> extract(File pdfFile) {
        List<String> strings = new ArrayList<String>();
        try {
            InputStream inputStream;
            inputStream = new FileInputStream(pdfFile);
            this.extractor.setPDF(inputStream);
            BxDocument bxDocument = this.extractor.getBxDocument();

            System.out.println(bxDocument.asLines());
            for (BxPage bxPage : bxDocument.asPages()) {

                // System.out.println("---------------");
                for (BxZone bxZone : bxPage) {
                    // System.out.println("---------");
                    for (BxLine bxLine : bxZone) {

                        // System.out.println("----");
                        // System.out.println(bxLine.toText());
                        // System.out.println(TextUtils.fixAccents(bxLine.toText()));
                        // System.out.println(bxLine.getX() + " : " +
                        // bxLine.getY());
                        strings.add(TextUtils.fixAccents(bxLine.toText()));

                    }
                }
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
        } catch (Exception e) {
            // TODO figure out why
            // InlineImageParseException/InvocationTargetException is not caught
            e.printStackTrace();
        }
        return strings;
    }

}
