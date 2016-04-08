package de.mkrnr.rse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Deserializer {

    public static Object deserialize(File serializedFile) {
        Object object = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedFile));
            object = in.readObject();
            in.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }
}
