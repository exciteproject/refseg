package de.exciteproject.refseg.util;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

public class GoddagUtils {

    private static final Gson GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonSerializer());
        gsonBuilder.registerTypeAdapter(Goddag.class, Goddag.getJsonDeserializer());
        gsonBuilder.registerTypeAdapter(Node.class, Node.getJsonSerializer());
        GSON = gsonBuilder.create();

    }

    public static Goddag deserializeGoddag(String goddagInputString) {
        return GSON.fromJson(goddagInputString, Goddag.class);
    }

    public static Goddag[] deserializeGoddags(String goddagInputString) {
        return GSON.fromJson(goddagInputString, Goddag[].class);
    }

    public static void main(String[] args) throws JsonSyntaxException, IOException {

        File goddagInputFile = new File(args[0]);

        Goddag[] deserializedGoddags = GoddagUtils.deserializeGoddags(FileUtils.readFile(goddagInputFile));

        for (Goddag goddag : deserializedGoddags) {
            System.out.println(goddag);
            System.out.println();
        }
    }

}
