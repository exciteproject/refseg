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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.exciteproject.refseg.util.CsvUtils;
import de.exciteproject.refseg.util.MapUtils;

public class PubMedReferenceFieldsExtractor {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        PubMedReferenceFieldsExtractor pubMedReferenceExtractor = new PubMedReferenceFieldsExtractor();

        // read the following file:
        // ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_non_comm_use_pdf.csv
        // pubMedReferenceExtractor.readPdfList(new File(args[0]));

        Map<String, Integer> authorCounts = new HashMap<String, Integer>();
        Map<String, Map<String, Integer>> countsMaps = new HashMap<String, Map<String, Integer>>();
        countsMaps.put("author", new HashMap<String, Integer>());
        countsMaps.put("article-title", new HashMap<String, Integer>());
        countsMaps.put("year", new HashMap<String, Integer>());
        countsMaps.put("source", new HashMap<String, Integer>());
        countsMaps.put("publisher-loc", new HashMap<String, Integer>());
        countsMaps.put("publisher-name", new HashMap<String, Integer>());
        // TODO combine volume and issue as well as fpage and lpage
        // countsMaps.put("volume", new HashMap<String, Integer>());
        // countsMaps.put("issue", new HashMap<String, Integer>());
        // countsMaps.put("fpage", new HashMap<String, Integer>());
        // countsMaps.put("lpage", new HashMap<String, Integer>());
        //
        for (String pubMedXmlFilePath : args) {
            TarArchiveInputStream tarInput = new TarArchiveInputStream(
                    new GzipCompressorInputStream(new FileInputStream(new File(pubMedXmlFilePath))));
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
                            refFieldValues = pubMedReferenceExtractor.getRefFieldValues(entry.getKey(), xmlContent);

                        }
                        for (String refFieldValue : refFieldValues) {
                            MapUtils.addCount(entry.getValue(), refFieldValue);
                        }
                    }

                    // System.out.println(currentEntry.getName());
                    count++;
                    if ((count % 10000) == 0) {
                        // System.out.println(count);
                        break;
                    }
                }
                currentEntry = tarInput.getNextTarEntry();

            }
            tarInput.close();
        }
        for (Entry<String, Map<String, Integer>> entry : countsMaps.entrySet()) {
            pubMedReferenceExtractor.writeToFile(entry.getValue(),
                    new File("/home/mkoerner/data/excite/test/" + entry.getKey() + ".csv"));

        }

    }

    private DocumentBuilder builder;

    public PubMedReferenceFieldsExtractor() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setNamespaceAware(true);
        this.builder = factory.newDocumentBuilder();

    }

    public List<String> getAuthorNames(String xmlContent) throws SAXException, IOException {
        List<String> authorNames = new ArrayList<String>();

        List<Node> citations = this.getCitations(xmlContent);
        for (Node citation : citations) {
            List<Node> nameNodes = new ArrayList<Node>();
            Node nameParentNode = citation;

            // set nameParentNode to <person-group person-group-type="author">
            // if existing
            Node personGroupNode = this.getNode("person-group", citation.getChildNodes());
            if (personGroupNode != null) {
                if ("author".equals(this.getNodeAttr("person-group-type", personGroupNode))) {
                    nameParentNode = personGroupNode;
                }
            }

            // add all names that don't have an additional <person-group> tag
            nameNodes.addAll(this.getNodes("name", nameParentNode.getChildNodes()));
            nameNodes.addAll(this.getNodes("string-name", nameParentNode.getChildNodes()));

            for (Node nameNode : nameNodes) {
                Node givenNamesNode = this.getNode("given-names", nameNode.getChildNodes());
                Node surNameNode = this.getNode("surname", nameNode.getChildNodes());
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
            Node articleTitleNode = this.getNode(refFieldName, citation.getChildNodes());
            if (articleTitleNode != null) {
                String normalizedField = CsvUtils.normalize(articleTitleNode.getTextContent());
                titles.add(normalizedField);
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
        Node backNode = this.getNode("back", root.getChildNodes());
        // System.out.println(backNode.getTextContent());
        if (backNode != null) {
            Node refListNode = this.getNode("ref-list", backNode.getChildNodes());
            if (refListNode != null) {
                List<Node> refNodes = this.getNodes("ref", refListNode.getChildNodes());
                for (Node refNode : refNodes) {
                    Node mixedCitationNode = this.getNode("mixed-citation", refNode.getChildNodes());
                    if (mixedCitationNode != null) {
                        citations.add(mixedCitationNode);
                    }
                    Node elementCitationNode = this.getNode("element-citation", refNode.getChildNodes());
                    if (elementCitationNode != null) {
                        citations.add(elementCitationNode);
                    }
                }
            }
        }
        return citations;
    }

    // TODO: move to helper class?
    private Node getNode(String tagName, NodeList nodes) {
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }

        return null;
    }

    private String getNodeAttr(String attrName, Node node) {
        NamedNodeMap attrs = node.getAttributes();
        for (int y = 0; y < attrs.getLength(); y++) {
            Node attr = attrs.item(y);
            if (attr.getNodeName().equalsIgnoreCase(attrName)) {
                return attr.getNodeValue();
            }
        }
        return "";
    }

    private List<Node> getNodes(String tagName, NodeList nodes) {
        List<Node> matches = new ArrayList<Node>();
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                matches.add(node);
            }
        }
        return matches;
    }

}
