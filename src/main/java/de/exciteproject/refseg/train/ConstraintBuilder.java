package de.exciteproject.refseg.train;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.mkrnr.goddag.Goddag;

//TODO refactor the whole class...
public class ConstraintBuilder {

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, IOException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
        Gson gson = gsonBuilder.create();
        // ConstraintBuilder constraintBuilder = new ConstraintBuilder();
        Positions positions = new Positions(true, false, false);
        String[] labelHierarchy = { "[A]", "[LN]" };
        // Counter counter = new DirectPositionCounter(labelHierarchy,
        // positions, "B-title");
        Counter counter = new ParentPositionCounter(labelHierarchy, positions, "B-LN");

        Goddag goddag = gson.fromJson(new FileReader("/home/mkoerner/tmp/names-tagged.json"), Goddag.class);
        ConstraintCounts constraintCounts = new ConstraintCounts();
        counter.count(goddag, constraintCounts);
        for (String word : constraintCounts.getWords()) {
            System.out.println(word + ": " + constraintCounts.getWordCounts(word));
        }

    }

    public void printContraints() {
        // for (Entry<String, NameDistribution> nameEntry :
        // this.nameDistributions.entrySet()) {
        // System.out.println(nameEntry.getKey());
        // System.out.println("\tA: " + nameEntry.getValue().authorCount);
        // System.out.println("\tO: " + nameEntry.getValue().otherCount);
        // }
    }

    /**
     * stores extracted author statistics in a file according to
     * cc.mallet.fst.semi_supervised.FSTConstraintUtil
     *
     * the file contains lines similar to: Friedrich A:0.8 O:0.2
     *
     * @param outputFile
     * @param authorLabel
     *            label for author distribution, is not allowed to contain
     *            colons
     * @param otherLabel
     *            label for other, is not allowed to contain colons
     * @throws IOException
     */
    public void writeDistributions(ConstraintCounts constraintCounts, File outputFile) throws IOException {
        // int nameCount = 0;
        // BufferedWriter bufferedWriter = new BufferedWriter(new
        // FileWriter(outputFile));
        // for (Entry<String, NameDistribution> nameEntry :
        // this.nameDistributions.entrySet()) {
        // String name = nameEntry.getKey();
        // if (name.contains(" ")) {
        // bufferedWriter.close();
        // throw new IllegalStateException("name contains space: " + name);
        // }

        // nameCount += 1;

        // double authorPercentage = nameEntry.getValue().authorCount /
        // nameEntry.getValue().getSum();
        // double otherPercentage = nameEntry.getValue().otherCount /
        // nameEntry.getValue().getSum();
        // String line = name;
        // line += " ";
        // line += authorLabel + ":" + authorPercentage;
        // line += " ";
        // line += otherLabel + ":" + otherPercentage;

        // line += System.lineSeparator();
        // bufferedWriter.write(line);
        // }
        // bufferedWriter.close();
        // return nameCount;
    }

}
