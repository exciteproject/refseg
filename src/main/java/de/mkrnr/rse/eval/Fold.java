package de.mkrnr.rse.eval;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
        // default deserialization
        objectInputStream.defaultReadObject();

        String foldAsString = (String) objectInputStream.readObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Fold fold = gson.fromJson(foldAsString, Fold.class);
        this.testingFiles = fold.getTestingFiles();
        this.trainingFiles = fold.getTrainingFiles();

    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        // default serialization
        objectOutputStream.defaultWriteObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        objectOutputStream.writeObject(gson.toJson(this));
    }
}
