package de.mkrnr.rse.distsup;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Name {
    public static void main(String[] args) {
        String[] firstNames = { "Markus", "Anton" };
        String[] lastNames = { "van", "Müller" };
        Name name = new Name(firstNames, lastNames);
        String[] variations = name.getVariations(true);
        for (String variation : variations) {
            System.out.println(variation);
        }
        System.out.println(variations.length);
    }

    private final String FIRST_NAME_TAG = "firstName";
    private final String LAST_NAME_TAG = "lastName";

    private final String AUTHOR_TAG = "author";

    private String[] firstNames;
    private String[] lastNames;

    public Name(String[] firstNames, String[] lastNames) {
        this.firstNames = firstNames;
        this.lastNames = lastNames;
    }

    public String getFDotNDotLastNames(boolean withXML) {
        String result = "";

        result += this.getFDotNDot(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getFDotSpaceNDotLastNames(boolean withXML) {
        String result = "";
        result += this.getFDotSpaceNDot(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getFirstNameLastNames(boolean withXML) {
        String result = "";
        result += this.getFirstName(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getFirstNamesLastNames(boolean withXML) {
        String result = "";
        result += this.getFirstNames(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getFirstNDotLastNames(boolean withXML) {
        String result = "";
        result += this.getFirstNDot(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getFirstNLastNames(boolean withXML) {
        String result = "";
        result += this.getFirstN(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getFNLastNames(boolean withXML) {
        String result = "";
        result += this.getFN(withXML);
        result += " ";
        result += this.getLastNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFDotNDot(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFDotNDot(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFDotSpaceNDot(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFDotSpaceNDot(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFirstN(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFirstN(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFirstName(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFirstName(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFirstNames(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFirstNames(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFirstNDot(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFirstNDot(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String getLastNamesCommaFN(boolean withXML) {
        String result = "";
        result += this.getLastNamesComma(withXML);
        result += " ";
        result += this.getFN(withXML);

        result = this.addAuthorTags(result, withXML);
        return result;
    }

    public String[] getVariations(boolean withXML) {
        Set<String> variations = new LinkedHashSet<String>();

        variations.add(this.getFirstNamesLastNames(withXML));
        variations.add(this.getFirstNDotLastNames(withXML));
        variations.add(this.getFirstNLastNames(withXML));
        variations.add(this.getFirstNameLastNames(withXML));
        variations.add(this.getFDotSpaceNDotLastNames(withXML));
        variations.add(this.getFDotNDotLastNames(withXML));
        variations.add(this.getFNLastNames(withXML));

        variations.add(this.getLastNamesCommaFirstNames(withXML));
        variations.add(this.getLastNamesCommaFirstNDot(withXML));
        variations.add(this.getLastNamesCommaFirstN(withXML));
        variations.add(this.getLastNamesCommaFirstName(withXML));
        variations.add(this.getLastNamesCommaFDotSpaceNDot(withXML));
        variations.add(this.getLastNamesCommaFDotNDot(withXML));
        variations.add(this.getLastNamesCommaFN(withXML));

        return variations.toArray(new String[0]);

    }

    public Map<String, String> getVariationsXMLMap() {
        Map<String, String> variationsXMLMap = new LinkedHashMap<String, String>();
        String[] variations = this.getVariations(false);
        String[] variationsWithXML = this.getVariations(true);
        for (int i = 0; i < variations.length; i++) {
            variationsXMLMap.put(variations[i], variationsWithXML[i]);
        }
        return variationsXMLMap;
    }

    private String addAuthorTags(String name, boolean withXML) {
        if (withXML) {
            name = "<" + this.AUTHOR_TAG + ">" + name + "</" + this.AUTHOR_TAG + ">";
        }
        return name;
    }

    private String addFirstNameTags(String name, boolean withXML) {
        if (withXML) {
            name = "<" + this.FIRST_NAME_TAG + ">" + name + "</" + this.FIRST_NAME_TAG + ">";
        }
        return name;
    }

    private String addLastNameTags(String name, boolean withXML) {
        if (withXML) {
            name = "<" + this.LAST_NAME_TAG + ">" + name + "</" + this.LAST_NAME_TAG + ">";
        }
        return name;
    }

    private String getFDotNDot(boolean withXML) {
        String result = "";
        for (String firstName : this.firstNames) {
            result += firstName.substring(0, 1) + ".";
        }

        result = this.addFirstNameTags(result, withXML);
        return result;
    }

    private String getFDotSpaceNDot(boolean withXML) {
        String result = "";
        for (String firstName : this.firstNames) {
            result += firstName.substring(0, 1) + ". ";
        }
        result = result.replaceFirst(" $", "");

        result = this.addFirstNameTags(result, withXML);
        return result;
    }

    private String getFirstN(boolean withXML) {
        String result = "";
        result += this.firstNames[0];
        if (this.firstNames.length > 1) {
            for (int i = 1; i < this.firstNames.length; i++) {
                result += " " + this.firstNames[i].substring(0, 1);
            }
        }
        result = this.addFirstNameTags(result, withXML);
        return result;
    }

    private String getFirstName(boolean withXML) {
        String result = "";
        result += this.firstNames[0];

        result = this.addFirstNameTags(result, withXML);
        return result;
    }

    private String getFirstNames(boolean withXML) {
        String result = "";
        for (String firstNames : this.firstNames) {
            result += firstNames + " ";
        }
        result = result.replaceFirst(" $", "");

        result = this.addFirstNameTags(result, withXML);
        return result;
    }

    private String getFirstNDot(boolean withXML) {
        String result = "";
        result += this.firstNames[0];
        if (this.firstNames.length > 1) {
            for (int i = 1; i < this.firstNames.length; i++) {
                result += " " + this.firstNames[i].substring(0, 1) + ".";
            }
        }
        result = this.addFirstNameTags(result, withXML);

        return result;
    }

    private String getFN(boolean withXML) {
        String result = "";
        for (String firstName : this.firstNames) {
            result += firstName.substring(0, 1);
        }

        result = this.addFirstNameTags(result, withXML);
        return result;
    }

    private String getLastNames(boolean withXML) {
        String result = "";
        for (String lastName : this.lastNames) {
            result += lastName + " ";
        }
        result = result.replaceFirst(" $", "");

        result = this.addLastNameTags(result, withXML);
        return result;
    }

    private String getLastNamesComma(boolean withXML) {
        String result = "";
        for (String lastName : this.lastNames) {
            result += lastName + " ";
        }
        result = result.replaceFirst(" $", "");
        result += ",";

        result = this.addLastNameTags(result, withXML);
        return result;
    }
}
