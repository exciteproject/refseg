package de.mkrnr.rse.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonHelper {

    public static Object readFromFile(Class<?> objectClass, File inputFile) {
        String jsonString = null;
        try {
            jsonString = FileUtils.readFileToString(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(jsonString, objectClass);

    }

    public static void writeToFile(Object object, File ouputFile) {

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtils.writeStringToFile(ouputFile, gson.toJson(object));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
