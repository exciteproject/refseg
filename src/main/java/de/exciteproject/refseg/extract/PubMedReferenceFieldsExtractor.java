package de.exciteproject.refseg.extract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import de.exciteproject.refseg.util.CsvUtils;
import de.exciteproject.refseg.util.MapUtils;
import de.exciteproject.refseg.util.XmlUtils;

public class PubMedReferenceFieldsExtractor {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        PubMedReferenceFieldsExtractor pubMedReferenceFieldsExtractor = new PubMedReferenceFieldsExtractor();

        JCommander jCommander;
        try {
            jCommander = new JCommander(pubMedReferenceFieldsExtractor, args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        if (pubMedReferenceFieldsExtractor.help) {
            jCommander.usage();
        } else {
            pubMedReferenceFieldsExtractor.run();
        }
    }

    @Parameter(names = { "-h", "--help" }, description = "print information about available parameters")
    private boolean help;
    @Parameter(description = "files that contain tar.gz pmc article files", converter = FileConverter.class, required = true)
    private List<File> inputFiles;
    @Parameter(names = { "-out",
            "--output-directory" }, description = "directory for the extracted files", converter = FileConverter.class, required = true)
    private File outputDirectory;

    private DocumentBuilder builder;

    private Set<String> personGroupTypeAttributes;

    public PubMedReferenceFieldsExtractor() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setNamespaceAware(true);
        this.builder = factory.newDocumentBuilder();

        this.personGroupTypeAttributes = new HashSet<String>();
        this.personGroupTypeAttributes.add("author");
        this.personGroupTypeAttributes.add("allauthors");
        this.personGroupTypeAttributes.add("");

    }

    public List<String> getAuthorNames(String xmlContent) throws SAXException, IOException {
        List<String> authorNames = new ArrayList<String>();

        List<Node> citations = this.getCitations(xmlContent);
        for (Node citation : citations) {
            List<Node> nameNodes = new ArrayList<Node>();
            Node nameParentNode = citation;

            // set nameParentNode to <person-group person-group-type="author">
            // if existing
            Node personGroupNode = XmlUtils.getNode("person-group", citation.getChildNodes());
            if (personGroupNode != null) {
                if (this.personGroupTypeAttributes
                        .contains(XmlUtils.getNodeAttr("person-group-type", personGroupNode))) {
                    nameParentNode = personGroupNode;
                }
            }

            // add all names that don't have an additional <person-group> tag
            nameNodes.addAll(XmlUtils.getNodes("name", nameParentNode.getChildNodes()));
            nameNodes.addAll(XmlUtils.getNodes("string-name", nameParentNode.getChildNodes()));

            for (Node nameNode : nameNodes) {
                Node givenNamesNode = XmlUtils.getNode("given-names", nameNode.getChildNodes());
                Node surNameNode = XmlUtils.getNode("surname", nameNode.getChildNodes());
                if ((givenNamesNode != null) && (surNameNode != null)) {
                    String givenNames = CsvUtils.normalize(givenNamesNode.getTextContent());
                    String surName = CsvUtils.normalize(surNameNode.getTextContent());
                    authorNames.add(givenNames + "\t" + surName);
                }
            }
        }

        return authorNames;
    }

    public List<String> getRefFieldValues(String refFieldName, String xmlContent) throws SAXException, IOException {
        List<String> titles = new ArrayList<String>();
        List<Node> citations = this.getCitations(xmlContent);
        for (Node citation : citations) {
            Node articleTitleNode = XmlUtils.getNode(refFieldName, citation.getChildNodes());
            if (articleTitleNode != null) {
                String normalizedField = CsvUtils.normalize(articleTitleNode.getTextContent());
                titles.add(normalizedField);
            }
        }
        return titles;
    }

    public List<String> getRefFieldValues(String[] refFieldNames, String xmlContent) throws SAXException, IOException {
        List<String> titles = new ArrayList<String>();
        List<Node> citations = this.getCitations(xmlContent);
        for (Node citation : citations) {
            String values = "";
            boolean found = true;
            for (String refFieldName : refFieldNames) {
                Node articleTitleNode = XmlUtils.getNode(refFieldName, citation.getChildNodes());
                if (articleTitleNode != null) {
                    String normalizedField = CsvUtils.normalize(articleTitleNode.getTextContent());
                    values += normalizedField + "\t";
                } else {
                    found = false;
                }
            }
            if (found) {
                values = values.replaceFirst("\t$", "");
                titles.add(values);
            }
        }
        return titles;
    }

    public void writeToFile(Map<String, Integer> map, File outputFile) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (Entry<String, Integer> mapEntry : map.entrySet()) {
            bufferedWriter.write(mapEntry.getKey() + "\t" + mapEntry.getValue());
            bufferedWriter.newLine();

        }
        bufferedWriter.close();
    }

    private List<Node> getCitations(String xmlContent) throws SAXException, IOException {
        List<Node> citations = new ArrayList<Node>();

        Document doc = this.builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        Node root = doc.getDocumentElement();
        Node backNode = XmlUtils.getNode("back", root.getChildNodes());
        // System.out.println(backNode.getTextContent());
        if (backNode != null) {
            Node refListNode = XmlUtils.getNode("ref-list", backNode.getChildNodes());
            if (refListNode != null) {
                List<Node> refNodes = XmlUtils.getNodes("ref", refListNode.getChildNodes());
                for (Node refNode : refNodes) {
                    Node mixedCitationNode = XmlUtils.getNode("mixed-citation", refNode.getChildNodes());
                    if (mixedCitationNode != null) {
                        citations.add(mixedCitationNode);
                    }
                    Node elementCitationNode = XmlUtils.getNode("element-citation", refNode.getChildNodes());
                    if (elementCitationNode != null) {
                        citations.add(elementCitationNode);
                    }
                }
            }
        }
        return citations;
    }

    private void run() throws IOException, ParserConfigurationException, SAXException {
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        PubMedReferenceFieldsExtractor pubMedReferenceExtractor = new PubMedReferenceFieldsExtractor();

        // read the following file:
        // ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_non_comm_use_pdf.csv
        // pubMedReferenceExtractor.readPdfList(new File(args[0]));

        Map<String, Map<String, Integer>> countsMaps = new HashMap<String, Map<String, Integer>>();
        countsMaps.put("author", new HashMap<String, Integer>());
        countsMaps.put("article-title", new HashMap<String, Integer>());
        countsMaps.put("year", new HashMap<String, Integer>());
        countsMaps.put("source", new HashMap<String, Integer>());
        countsMaps.put("publisher-loc", new HashMap<String, Integer>());
        countsMaps.put("publisher-name", new HashMap<String, Integer>());
        countsMaps.put("volume_issue", new HashMap<String, Integer>());
        countsMaps.put("fpage_lpage", new HashMap<String, Integer>());
        //
        for (File pubMedXmlInputFile : this.inputFiles) {
            TarArchiveInputStream tarInput = new TarArchiveInputStream(
                    new GzipCompressorInputStream(new FileInputStream(pubMedXmlInputFile)));
            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            int count = 0;
            while (currentEntry != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(tarInput)); // Read
                if (currentEntry.isFile()) {
                    String xmlContent = "";
                    // TODO find way of directly inputting the TarInput to
                    // builder.parse (seems to close the InputStream?)
                    String line;
                    while ((line = br.readLine()) != null) {
                        xmlContent += line + "\n";
                    }

                    for (Entry<String, Map<String, Integer>> entry : countsMaps.entrySet()) {
                        List<String> refFieldValues;
                        if (entry.getKey().equals("author")) {
                            refFieldValues = pubMedReferenceExtractor.getAuthorNames(xmlContent);
                        } else {
                            // TODO find better solution for multiple fields
                            if (entry.getKey().split("_").length == 2) {
                                refFieldValues = pubMedReferenceExtractor.getRefFieldValues(entry.getKey().split("_"),
                                        xmlContent);
                            } else {
                                refFieldValues = pubMedReferenceExtractor.getRefFieldValues(entry.getKey(), xmlContent);
                            }
                        }
                        for (String refFieldValue : refFieldValues) {
                            MapUtils.addCount(entry.getValue(), refFieldValue);
                        }
                    }

                    count++;
                    if ((count % 1000) == 0) {
                        System.out.println(count);
                        // TODO remove
                        break;
                    }
                }
                currentEntry = tarInput.getNextTarEntry();

            }
            tarInput.close();
        }
        for (Entry<String, Map<String, Integer>> entry : countsMaps.entrySet()) {
            pubMedReferenceExtractor.writeToFile(entry.getValue(),
                    new File(this.outputDirectory.getAbsolutePath() + "/" + entry.getKey() + ".csv"));

        }
    }

}
