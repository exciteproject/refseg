package de.exciteproject.refseg.extract;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

/**
 * Class for downloading pubmed pdfs from the public FTP server.
 */
public class PubMedPdfDownloader {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        PubMedPdfDownloader pubMedReferenceExtractor = new PubMedPdfDownloader();

        // read the following file:
        // ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_non_comm_use_pdf.csv
        pubMedReferenceExtractor.readPdfList(new File(args[0]));

        // System.out.println(pubMedReferenceExtractor.pdfMap.get("4040706"));

        pubMedReferenceExtractor.downloadPdfs(new File(args[1]));

    }

    private final String pubmedPrefix = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/";

    // Map<PMCID,FTPLink>
    private Map<String, String> pdfMap;

    // TODO add parameters
    private int connectionTimeout = 10000;
    private int readTimeout = 60000;

    public void readPdfList(File file) throws IOException, ParserConfigurationException {
        this.pdfMap = new HashMap<String, String>();
        CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
        for (CSVRecord csvRecord : parser) {
            this.pdfMap.put(csvRecord.get(2).substring(3), csvRecord.get(0));
        }
    }

    private void downloadPdfs(File outputDir) {
        for (Entry<String, String> pdfMapEntry : this.pdfMap.entrySet()) {
            File outputFile = new File(outputDir.getAbsolutePath() + "/" + pdfMapEntry.getKey() + ".pdf");
            if (outputFile.exists()) {
                continue;
            }
            System.out.println(pdfMapEntry.getValue());
            try {
                FileUtils.copyURLToFile(new URL(this.pubmedPrefix + pdfMapEntry.getValue()), outputFile,
                        this.connectionTimeout, this.readTimeout);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
