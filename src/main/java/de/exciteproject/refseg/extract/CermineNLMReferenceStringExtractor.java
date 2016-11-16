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
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

/**
 * Class for extracting references in NLM xml format using CERMINE.
 * <p>
 * TODO: refactor: combine with CermineReferenceStringExtractor
 */
public class CermineNLMReferenceStringExtractor extends ReferenceStringExtractor {

    public static void main(String[] args) throws IOException, AnalysisException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (File file : inputDir.listFiles()) {
            System.out.println(file.getAbsolutePath());
            CermineNLMReferenceStringExtractor cermineReferenceStringExtractor = new CermineNLMReferenceStringExtractor();

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

    public CermineNLMReferenceStringExtractor() throws AnalysisException {
        this.extractor = new ContentExtractor();
    }

    @Override
    public List<String> extract(File pdfFile) {
        List<String> references = new ArrayList<String>();
        try {
            InputStream inputStream;
            inputStream = new FileInputStream(pdfFile);
            this.extractor.setPDF(inputStream);
            List<Element> referenceElements = this.extractor.getReferencesAsNLM();

            XMLOutputter outp = new XMLOutputter();
            String s = outp.outputString(referenceElements);
            System.out.println(s);
            // for (Element element : result) {
            // XMLOutputter outp = new XMLOutputter();
            // String s = outp.outputString(element);
            // System.out.println(s);
            // System.out.println("----");
            // }

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
        return references;
    }

}
