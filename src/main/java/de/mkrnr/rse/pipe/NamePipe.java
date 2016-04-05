package de.mkrnr.rse.pipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * A Pipe that adds a feature with the value true if the token was found in a
 * data base for last names.
 */
public class NamePipe extends Pipe implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int CURRENT_SERIAL_VERSION = 1;

    private String featureName;

    private HashSet<String> lastNames;

    /**
     *
     * @param featureName
     *            the label that is used when a last name was detected
     * @param lastNameFile
     *            a file containing one last name per line
     */
    public NamePipe(String featureName, File lastNameFile) {
        this.featureName = featureName;

        this.lastNames = new HashSet<String>();
        try {
            BufferedReader lastNameFileReader = new BufferedReader(new FileReader(lastNameFile));
            String line;
            while ((line = lastNameFileReader.readLine()) != null) {
                this.lastNames.add(line.trim());
            }
            lastNameFileReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("lastNameFile not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Instance pipe(Instance instance) {

        TokenSequence sequence = (TokenSequence) instance.getData();

        for (Token token : sequence) {
            String[] tokenSplit = token.getText().trim().split("\\s");
            for (String tokenPart : tokenSplit) {
                String normalizedTokenPart = tokenPart.replaceAll("[^\\p{L}]", "");
                if ((normalizedTokenPart.length() > 0) && this.lastNames.contains(normalizedTokenPart)) {
                    token.setFeatureValue(this.featureName, 1.0);
                }
            }
        }
        return instance;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.featureName = (String) in.readObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.featureName);
    }

}
