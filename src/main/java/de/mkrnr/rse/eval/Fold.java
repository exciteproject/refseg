package de.mkrnr.rse.eval;

import java.io.File;
import java.util.ArrayList;

public class Fold {

    private ArrayList<File> trainingFiles;

    private ArrayList<File> testingFiles;

    public Fold() {
        this.trainingFiles = new ArrayList<File>();
        this.testingFiles = new ArrayList<File>();
    }

    public boolean addTestingFile(File file) {
        return this.testingFiles.add(file);
    }

    public boolean addTrainingFile(File file) {
        return this.trainingFiles.add(file);
    }

    public ArrayList<File> getTestingFiles() {
        return this.testingFiles;
    }

    public ArrayList<File> getTrainingFiles() {
        return this.trainingFiles;
    }

    // TODO Add serialization
}
