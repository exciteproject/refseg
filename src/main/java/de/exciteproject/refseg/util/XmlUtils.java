package de.exciteproject.refseg.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {

    private static final Pattern XML_TAGS_PATTERN = Pattern.compile("<([^<>]+)>([^<>]+)</([^<>]+)>");

    // matches names in xml tags including characters before and after until
    // the first space (if existing)
    private static final Pattern XML_TAGS_WITH_CONTEXT_PATTERN = Pattern
            .compile("\\s?([^\\s]*)" + XmlUtils.XML_TAGS_PATTERN.pattern() + "([^\\s]*)\\s?");

    public static Matcher getXMLTagMatcher(String inputString) {
        return XmlUtils.XML_TAGS_PATTERN.matcher(inputString);
    }

    public static Matcher getXMLTagWithContextMatcher(String inputString) {
        return XmlUtils.XML_TAGS_WITH_CONTEXT_PATTERN.matcher(inputString);
    }

    public static Document loadXMLFromString(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public static String removeTags(String input) {
        String result = input;

        result = result.replaceAll("<([^<>]+)>", "");

        return result;
    }
}
