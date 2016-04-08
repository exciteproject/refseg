package de.mkrnr.rse.eval;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Fold implements Serializable {

    private static final long serialVersionUID = 6930092131586375404L;

    private ArrayList<File> testingFiles;
    private ArrayList<File> trainingFiles;

    private String foldName;

    public Fold(String foldName) {
        this.foldName = foldName;

        this.testingFiles = new ArrayList<File>();
        this.trainingFiles = new ArrayList<File>();
    }

    public boolean addTestingFile(File file) {
        return this.testingFiles.add(file);
    }

    public boolean addTrainingFile(File file) {
        return this.trainingFiles.add(file);
    }

    public String getName() {
        return this.foldName;
    }

    public ArrayList<File> getTestingFiles() {
        return this.testingFiles;
    }

    public ArrayList<File> getTrainingFiles() {
        return this.trainingFiles;
    }

    public void printFoldInformation() {
        System.out.println("Name: " + this.foldName);
        System.out.println("File directory: " + this.trainingFiles.get(0).getParent());
        System.out.print("Training files: ");
        for (File file : this.trainingFiles) {
            System.out.print(file.getName() + "\t");
        }

        System.out.println();

        System.out.print("Testing files: ");
        for (File file : this.testingFiles) {
            System.out.print(file.getName() + "\t");
        }
        System.out.println();
    }

}
