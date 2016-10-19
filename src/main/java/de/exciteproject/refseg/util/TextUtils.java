package de.exciteproject.refseg.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class TextUtils {
    private static Pattern accentPattern = Pattern.compile("\\w(´|¨)(\\s)?");

    public static String fixAccents(String input) {

        String output = input;
        Matcher matcher = accentPattern.matcher(output);

        int startIndex = 0;
        while (matcher.find(startIndex)) {
            // can't set startIndex to matcher.end() since the output length
            // changes after a replacement
            startIndex = matcher.start() + 1;

            String sequenceToReplace = matcher.group();

            String htmlChar = "&" + sequenceToReplace.charAt(0);
            switch (sequenceToReplace.charAt(1)) {
            case '¨':
                htmlChar += "uml";
                break;
            case '´':
                htmlChar += "acute";
                break;
            default:
                continue;

            }
            htmlChar += ";";

            String unescapedChar = StringEscapeUtils.unescapeHtml4(htmlChar);

            // if HTML string was escaped (is valid HTML)
            if (!htmlChar.equals(unescapedChar)) {
                output = output.replaceAll(sequenceToReplace, unescapedChar);
                matcher.reset(output);
            }
        }
        return output;
    }

    public static void main(String args[]) {
        String input = "A´ cs, Bo¨ hm,  Ra¨tsch, Bh¨ hm BA¨u¨ hm, Bo¨ hm Bo¨ hm ";
        System.out.println(TextUtils.fixAccents(input));
        // output: Ács, Böhm, Rätsch, Bh¨ hm BÄühm, Böhm Böhm
    }
}
