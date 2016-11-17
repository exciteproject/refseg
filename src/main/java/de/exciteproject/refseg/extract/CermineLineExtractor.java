package de.exciteproject.refseg.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refseg.util.FileUtils;
import de.exciteproject.refseg.util.TextUtils;
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

    /**
     *
     * @param args
     *            args[0]: directory containing pdf files and/or subfolders with
     *            pdf files </br>
     *            args[1]: directory in which the outputfiles are stored,
     *            including the subdirectories
     *
     * @throws IOException
     * @throws AnalysisException
     */
    public static void main(String[] args) throws IOException, AnalysisException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<File> inputFiles = FileUtils.listFilesRecursively(inputDir);
        CermineLineExtractor cermineReferenceStringExtractor = new CermineLineExtractor();

        Instant start = Instant.now();
        for (File inputFile : inputFiles) {
            System.out.println("processing: " + inputFile);

            File currentOutputDirectory;

            String subDirectories = inputFile.getParentFile().getAbsolutePath().replaceFirst(inputDir.getAbsolutePath(),
                    "");
            currentOutputDirectory = new File(outputDir.getAbsolutePath() + File.separator + subDirectories);

            String outputFileName = FilenameUtils.removeExtension(inputFile.getName()) + ".txt";
            File outputFile = new File(currentOutputDirectory.getAbsolutePath() + File.separator + outputFileName);

            // skip computation if outputFile already exists
            if (outputFile.exists()) {
                continue;
            }

            if (!currentOutputDirectory.exists()) {
                currentOutputDirectory.mkdirs();
            }
            cermineReferenceStringExtractor.extract(inputFile, outputFile);
        }
        Instant end = Instant.now();
        System.out.println("Done. Execution time: " + Duration.between(start, end));
    }

    private CerminePdfExtractor cerminePdfExtractor;

    public CermineLineExtractor() throws AnalysisException {
        this.cerminePdfExtractor = new CerminePdfExtractor();
    }

    /**
     * Extracts lines of text from a PDF file in the correct reading order.
     *
     * @param pdfFile
     * @param outputFile
     */
    public void extract(File pdfFile, File outputFile) {
        try {
            BxDocument bxDocument = this.cerminePdfExtractor.extractWithResolvedReadingOrder(pdfFile);

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

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
                        String fixedString = TextUtils.fixAccents(bxLine.toText());
                        // System.out.println(fixedString);
                        bufferedWriter.write(fixedString);
                        bufferedWriter.newLine();

                    }
                }
                // adds an empty line at the end of a page
                // bufferedWriter.newLine();

            }
            bufferedWriter.close();
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
    }

}
